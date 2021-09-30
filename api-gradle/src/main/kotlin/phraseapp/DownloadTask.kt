package phraseapp

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.printers.FileOperationImpl
import phraseapp.network.PhraseAppNetworkDataSource
import phraseapp.repositories.operations.Downloader

abstract class DownloadTask : DefaultTask() {
    @get:Input
    abstract val baseUrl: Property<String>

    @get:Input
    abstract val authToken: Property<String>

    @get:Input
    abstract val projectId: Property<String>

    @get:Input
    abstract val resFolders: Property<Map<String, List<String>>>

    @get:Input
    abstract val platform: Property<Platform>

    @get:Input
    abstract val output: Property<String>

    @get:Input
    abstract val localeNameRegex: Property<String>

    @get:Input
    abstract val overrideDefaultFile: Property<Boolean>

    @get:Input
    abstract val exceptions: Property<Map<String, String>>

    @get:Input
    abstract val placeholder: Property<Boolean>

    init {
        overrideDefaultFile.convention(false)
        exceptions.convention(emptyMap())
        placeholder.convention(false)
    }

    @TaskAction
    fun download() {
        var throwable: Throwable? = null
        val network = PhraseAppNetworkDataSource.newInstance(
            baseUrl.get(),
            authToken.get(),
            projectId.get(),
            platform.get().format
        )
        val fileOperation: FileOperation = FileOperationImpl()

        Downloader(platform.get(), output.get(), fileOperation, network)
            .download(
                resFolders.get(),
                overrideDefaultFile.get(),
                exceptions.get(),
                placeholder.get(),
                localeNameRegex.get()
            )
            .subscribe({
                logger.info("All resources have been printed!")
            }, {
                throwable = it
            })
        if (throwable != null) {
            throw GradleException("Something wrong happened during the downloading", throwable!!)
        }
    }
}