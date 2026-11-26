package fr.insalyon.creatis.moteurlite;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import fr.insalyon.creatis.moteurlite.util.DownloadUtils;


public class ContentDispositionParserTest {

    @Test
    public void parsesSimpleFilename() {
        assertEquals("report.pdf",
                DownloadUtils.getFilename("attachment; filename=report.pdf"));
    }

    @Test
    public void parsesQuotedFilename() {
        assertEquals("with space report.pdf",
                DownloadUtils.getFilename(
                        "attachment; filename=\"with space report.pdf\""));
    }

    @Test
    public void preservesSemicolonInsideQuotedFilename() {
        assertEquals("report; final.pdf",
                DownloadUtils.getFilename(
                        "attachment; filename=\"report; final.pdf\""));
    }

    @Test
    public void decodesUtf8FilenameStar() {
        assertEquals("café.pdf",
                DownloadUtils.getFilename(
                        "attachment; filename*=UTF-8''caf%C3%A9.pdf"));
    }

    @Test
    public void preservesPlusInFilenameStar() {
        assertEquals("a+b.pdf",
                DownloadUtils.getFilename(
                        "attachment; filename*=UTF-8''a+b.pdf"));
    }

    @Test
    public void prefersFilenameStarOverFilename() {
        assertEquals("actual.pdf",
                DownloadUtils.getFilename(
                        "attachment; filename=\"fallback.pdf\"; filename*=UTF-8''actual.pdf"));
    }

    @Test
    public void fallsBackWhenFilenameStarIsInvalid() {
        assertAll(
            () -> assertEquals("fallback1.txt", DownloadUtils.getFilename("attachment; filename=\"fallback1.txt\"; filename*=bogus''file.txt")),    // unknown charset
            () -> assertEquals("fallback2.txt", DownloadUtils.getFilename("attachment; filename=\"fallback2.txt\"; filename*=UTF-8'file.txt")),     // missing second quote
            () -> assertEquals("fallback3.txt", DownloadUtils.getFilename("attachment; filename=\"fallback3.txt\"; filename*=UTF-8''%E2%82%")),     // truncated percent-encoding
            () -> assertEquals("fallback4.txt", DownloadUtils.getFilename("attachment; filename=\"fallback4.txt\"; filename*=UTF-8''%ZZ.txt"))      // invalid hex digits
        );
    }

    @Test
    public void parameterNameIsCaseInsensitive() {
        assertEquals("report.pdf",
                DownloadUtils.getFilename(
                        "attachment; FILENAME=\"report.pdf\""));
    }

    @Test
    public void returnsNullForNullHeader() {
        assertNull(DownloadUtils.getFilename(null));
    }

    @Test
    public void returnsNullWhenEmptyHeader() {
        assertNull(DownloadUtils.getFilename(""));
    }

    @Test
    public void returnsNullWhenFilenameIsMissing() {
        assertNull(DownloadUtils.getFilename("attachment"));
    }

    @Test
    public void throwsParseExceptionForWrongFormat() {
        //TODO add more edge case
        assertThrows(IllegalArgumentException.class, () -> DownloadUtils.getFilename("azeaze\";;'"));
    }

}
