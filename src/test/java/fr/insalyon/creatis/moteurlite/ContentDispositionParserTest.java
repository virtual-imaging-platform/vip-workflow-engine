package fr.insalyon.creatis.moteurlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertEquals("quarterly report.pdf",
                DownloadUtils.getFilename(
                        "attachment; filename=\"quarterly report.pdf\""));
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
                        "attachment; filename=\"fallback.pdf\"; "
                                + "filename*=UTF-8''actual.pdf"));
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
    public void returnsNullWhenFilenameIsMissing() {
        assertNull(DownloadUtils.getFilename("attachment"));
    }

    @Test
    public void fallsBackWhenFilenameStarIsInvalid() {
        assertEquals("fallback.pdf",
                DownloadUtils.getFilename(
                        "attachment; filename=\"fallback.pdf\"; filename*=bad"));
    }
}
