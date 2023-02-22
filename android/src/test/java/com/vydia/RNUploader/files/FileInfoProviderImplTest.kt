package com.vydia.RNUploader.files

import com.vydia.RNUploader.files.helpers.FilesHelper
import com.vydia.RNUploader.files.helpers.MimeTypeHelper
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

@RunWith(JUnit4::class)
class FileInfoProviderImplTest {

    @Mock
    private lateinit var fileInfoObtainedCallback: (FileInfo) -> Unit

    @Mock
    private lateinit var exceptionReceivedCallback: (Exception) -> Unit

    @Mock
    private lateinit var mimeTypeHelper: MimeTypeHelper

    @Mock
    private lateinit var filesHelper: FilesHelper

    private lateinit var fileInfoProvider: FileInfoProvider

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        fileInfoProvider = FileInfoProviderImpl(
            mimeTypeHelper,
            filesHelper
        )
    }

    @Test
    fun `getFileInfo with non-null path`() {
        val path = "/path/to/file.txt"

        whenever(mimeTypeHelper.getFileExtensionFromUrl(path)).doReturn("txt")
        whenever(mimeTypeHelper.getMimeTypeFromExtension("txt")).doReturn("text/plain")
        whenever(filesHelper.getFileName(path)).doReturn("file.txt")
        whenever(filesHelper.getFileSize(path)).doReturn(1L)
        whenever(filesHelper.fileExists(path)).doReturn(true)

        fileInfoProvider.getFileInfo(
            path,
            fileInfoObtainedCallback,
            exceptionReceivedCallback
        )

        // Verify that onFileInfoObtained callback was called with correct FileInfo object
        val fileInfoArgumentCaptor = argumentCaptor<FileInfo>()
        verify(fileInfoObtainedCallback).invoke(fileInfoArgumentCaptor.capture())
        val fileInfo = fileInfoArgumentCaptor.firstValue
        assertEquals("file.txt", fileInfo.fileName)
        assertEquals("txt", fileInfo.extension)
        assertEquals("text/plain", fileInfo.mimeType)
        assertTrue(fileInfo.exists)
        assertTrue(fileInfo.fileSize.toLong() > 0L)

        // Verify that onExceptionReceived callback was not called
        verifyNoInteractions(exceptionReceivedCallback)
    }

    @Test
    fun `getFileInfo with null path`() {
        fileInfoProvider.getFileInfo(
            path = null,
            onFileInfoObtained = fileInfoObtainedCallback,
            onExceptionReceived = exceptionReceivedCallback
        )

        val fileInfoArgumentCaptor = argumentCaptor<Exception>()

        verify(exceptionReceivedCallback).invoke(fileInfoArgumentCaptor.capture())
        verifyNoInteractions(fileInfoObtainedCallback)
    }
}