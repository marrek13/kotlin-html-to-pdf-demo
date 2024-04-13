package pl.marrek13.kotlinhtmltopdfdemo.config

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import org.apache.commons.pool2.PooledObjectFactory
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.marrek13.kotlinhtmltopdfdemo.factory.BrowserContextPooledObjectFactory
import java.time.Duration

@Configuration
@ConditionalOnClass(Playwright::class, PooledObjectFactory::class)
class PlaywrightConfig{
    @Bean
    fun browserContextPool() =
        BrowserContextPool(
            BrowserContextPooledObjectFactory(),
            GenericObjectPoolConfig<BrowserContext>().also {
                it.jmxEnabled = false
                it.minIdle = 5
                it.maxIdle = 10
                it.maxTotal = 15
                it.softMinEvictableIdleDuration = Duration.ofMinutes(3)
                it.timeBetweenEvictionRuns = Duration.ofSeconds(30)
            },
        ).also {
            it.addObjects(it.minIdle)
        }

    class BrowserContextPool(
        factory: BrowserContextPooledObjectFactory,
        config: GenericObjectPoolConfig<BrowserContext>,
    ) : GenericObjectPool<BrowserContext>(factory, config)
}
