// frequentWordsCounter.js

document.getElementById("checkButton").addEventListener("click", () => {
    let input = document.getElementById("input").value;
    if (!input.trim()) {
        alert("Παρακαλώ εισάγετε κάποιο κείμενο.");
        return;
    }

    // Διαχωρισμός των λέξεων και αφαίρεση σημείων στίξης
    let words = input.toLowerCase().match(/\b\w{4,}\b/g);
    if (!words || words.length === 0) {
        alert("Δεν βρέθηκαν λέξεις με 4 ή περισσότερα γράμματα.");
        return;
    }

    // Καταμέτρηση συχνότητας λέξεων
    let wordCounts = {};
    words.forEach(word => {
        wordCounts[word] = (wordCounts[word] || 0) + 1;
    });

    // Εύρεση της πιο συχνής λέξης
    let mostFrequentWord = Object.keys(wordCounts).reduce((a, b) =>
        wordCounts[a] > wordCounts[b] ? a : b
    );

    alert(`Η πιο συχνή λέξη είναι: "${mostFrequentWord}" με ${wordCounts[mostFrequentWord]} εμφανίσεις.`);
});
