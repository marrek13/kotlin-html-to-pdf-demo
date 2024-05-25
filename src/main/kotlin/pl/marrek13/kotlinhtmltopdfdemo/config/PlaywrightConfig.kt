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
            GenericObjectPoolConfig<BrowserContext>().apply {
                jmxEnabled = false
                minIdle = 5
                maxIdle = 10
                maxTotal = 15
                softMinEvictableIdleDuration = Duration.ofMinutes(3)
                timeBetweenEvictionRuns = Duration.ofSeconds(30)
            },
        ).also {
            it.addObjects(it.minIdle)
        }

    class BrowserContextPool(
        factory: BrowserContextPooledObjectFactory,
        config: GenericObjectPoolConfig<BrowserContext>,
    ) : GenericObjectPool<BrowserContext>(factory, config)
}
