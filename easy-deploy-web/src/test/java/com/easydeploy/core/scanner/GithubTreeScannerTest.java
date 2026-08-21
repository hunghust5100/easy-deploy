package com.easydeploy.core.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GithubTreeScannerTest {

    @Test
    public void testParseStandardGithubUrl() {
        GithubTreeScanner scanner = new GithubTreeScanner();
        GithubTreeScanner.ParsedGithubUrl parsed = scanner.parseGithubUrlDetailed("https://github.com/Kyyaaaa/UmiCV");

        assertEquals("Kyyaaaa", parsed.getOwner());
        assertEquals("UmiCV", parsed.getRepo());
        assertNull(parsed.getBranch());
        assertEquals("https://github.com/Kyyaaaa/UmiCV.git", parsed.getCleanCloneUrl());
    }

    @Test
    public void testParseTreeBranchGithubUrl() {
        GithubTreeScanner scanner = new GithubTreeScanner();
        GithubTreeScanner.ParsedGithubUrl parsed = scanner.parseGithubUrlDetailed("https://github.com/Kyyaaaa/UmiCV/tree/main");

        assertEquals("Kyyaaaa", parsed.getOwner());
        assertEquals("UmiCV", parsed.getRepo());
        assertEquals("main", parsed.getBranch());
        assertEquals("https://github.com/Kyyaaaa/UmiCV.git", parsed.getCleanCloneUrl());
    }

    @Test
    public void testParseBlobBranchGithubUrl() {
        GithubTreeScanner scanner = new GithubTreeScanner();
        GithubTreeScanner.ParsedGithubUrl parsed = scanner.parseGithubUrlDetailed("https://github.com/Kyyaaaa/UmiCV/blob/develop/package.json");

        assertEquals("Kyyaaaa", parsed.getOwner());
        assertEquals("UmiCV", parsed.getRepo());
        assertEquals("develop", parsed.getBranch());
        assertEquals("https://github.com/Kyyaaaa/UmiCV.git", parsed.getCleanCloneUrl());
    }
}
