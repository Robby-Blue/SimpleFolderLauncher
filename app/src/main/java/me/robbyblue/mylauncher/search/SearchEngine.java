package me.robbyblue.mylauncher.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import me.robbyblue.mylauncher.files.FileNode;

public class SearchEngine {

    // TODO: add more here
    // like "YouTube" -> "yt"
    public ArrayList<NamedItem> indexSearchableItem(FileNode fileNode) {
        ArrayList<NamedItem> items = new ArrayList<>();
        items.add(new NamedItem(fileNode.getName(), fileNode));

        // initials "Clash Royale" -> "cr"
        String[] words = fileNode.getName().split(" ");
        if (words.length > 1) {
            StringBuilder initials = new StringBuilder();
            for (String word : words)
                initials.append(word.charAt(0));
            items.add(new NamedItem(initials.toString(), fileNode));
        }
        return items;
    }

    // TODO: make better
    // obvious idea: after finding matches, sort by how much they match
    // also consider usage when sorting if possible
    public ArrayList<SearchResult> searchFiles(String query, ArrayList<NamedItem> searchableFileItems) {
        query = query.toLowerCase();

        ArrayList<FileMatchResult> results = new ArrayList<>();

        for (NamedItem namedItem : searchableFileItems) {
            FileMatchResult result = matchesQuery(namedItem, query);
            if (result.getPoints() == 0) continue;
            if (results.contains(result)) {
                FileMatchResult currentResult = results.get(results.indexOf(result));
                if (result.getPoints() > currentResult.getPoints())
                    currentResult.updatePoints(result.getPoints());
                continue;
            }
            results.add(result);
        }

        results.sort(Comparator.comparing(FileMatchResult::getPoints, Comparator.reverseOrder()));
        results = new ArrayList<>(results.subList(0, Math.min(10, results.size())));

        ArrayList<SearchResult> searchResults = new ArrayList<>(10);

        for (FileMatchResult result : results) {
            searchResults.add(new FileSearchResult(result.getFileNode()));
        }

        return searchResults;
    }

    // TODO: probably add more
    private FileMatchResult matchesQuery(NamedItem namedItem, String query) {
        String name = namedItem.getName();
        FileNode fileNode = namedItem.getFileNode();

        FileMatchResult result = new FileMatchResult(fileNode);

        if (name.equals(query)) result.updatePoints(100);
        if (name.contains(query)) result.updatePoints(50);
        if (name.startsWith(query)) result.updatePoints(75);

        for (String word : name.split(" ")) {
            if (word.startsWith(query)) result.updatePoints(25);
        }

        return result;
    }

    ArrayList<SearchResult> searchDots(String query) {
        // TODO: implement this
        return null;
    }

    private static class FileMatchResult {

        private final FileNode fileNode;
        private int points;

        private FileMatchResult(FileNode fileNode) {
            this.fileNode = fileNode;
            this.points = 0;
        }

        public void updatePoints(int points) {
            if (points > this.points) {
                this.points = points;
            }
        }

        public FileNode getFileNode() {
            return fileNode;
        }

        public int getPoints() {
            return points;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileMatchResult that = (FileMatchResult) o;
            return Objects.equals(getFileNode(), that.getFileNode());
        }

    }

}
