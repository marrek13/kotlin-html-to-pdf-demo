package pl.marrek13.kotlinhtmltopdfdemo.factory

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.PooledObjectFactory
import org.apache.commons.pool2.impl.DefaultPooledObject
import java.util.concurrent.ConcurrentHashMap

class BrowserContextPooledObjectFactory : PooledObjectFactory<BrowserContext>, AutoCloseable {
    // this is a pool storage
    private val playwrightMap = ConcurrentHashMap<BrowserContext, Playwright>()

    // this method is executed after "borrowObject" method call on the pool
    override fun activateObject(p: PooledObject<BrowserContext>) {
        p.getObject()?.clearCookies()
    }

    // this method is executed when the pool evicts idle object
    override fun destroyObject(p: PooledObject<BrowserContext?>) {
        p.getObject()?.run {
            playwrightMap.remove(this)?.close()
        }
    }

    private fun cleanupBrowserContext(browserContext: BrowserContext) {
        browserContext.clearCookies()
        browserContext.clearPermissions()
        val pages = browserContext.pages()
        if (pages.isNotEmpty()) {
            for (page in pages) {
                if (page.isClosed) {
                    continue
                }
            }
        }
    }

    // this method is used to create a new instance of the object, which is added to the pool
    override fun makeObject(): PooledObject<BrowserContext> {
        val playwright = Playwright.create()
        val browserContext = playwright.chromium().launch().newContext()
        playwrightMap[browserContext] = playwright
        return DefaultPooledObject(browserContext)
    }

    // this method is called after "returnObject" method call on the pool
    override fun passivateObject(p: PooledObject<BrowserContext>) {
        p.getObject()?.run {
            clearCookies()
            pages().forEach { page ->
                page?.takeIf { !it.isClosed }?.close()
            }
        }
    }

    // checks if the object can be safely borrowed
    override fun validateObject(p: PooledObject<BrowserContext>) = p.getObject() != null

    // executed to close the whole pool of objects, usually during the app shutdown
    override fun close() =
        playwrightMap.forEach { (browserContext, playwright) ->
            cleanupBrowserContext(browserContext)
            playwright.close()
        }
}
