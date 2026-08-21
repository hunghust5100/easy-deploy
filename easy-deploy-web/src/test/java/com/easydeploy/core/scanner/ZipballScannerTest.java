package com.easydeploy.core.scanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ZipballScannerTest {

    @Test
    public void testScanZipball() {
        GithubTreeScanner scanner = new GithubTreeScanner();
        GithubTreeScanner.ScanResult result = scanner.scanGithubViaZipball("hunghust5100", "vtit-miniproject", "main");
        assertNotNull(result);
        System.out.println("Branch found: " + result.getDefaultBranch());
        System.out.println("Total files: " + result.getFilePaths().size());
        System.out.println("Key files: " + result.getKeyFilesContent().keySet());
        assertTrue(result.getFilePaths().size() > 10);
    }
}
