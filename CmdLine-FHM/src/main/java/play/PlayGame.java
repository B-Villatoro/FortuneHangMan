package play;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import constant.WordConstant;
import model.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.*;

public class PlayGame {

    private static final Scanner scan = new Scanner(System.in);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    // support async or sync connection
    final static CloseableHttpClient httpClient = HttpClients.createDefault();

    private static Game game = new Game();
    private static List<Team> teamList = new ArrayList<>();
    private static Team team = new Team();
    private static List<RoundsInGame> roundList = new ArrayList<>();
    private static final Round round = new Round();
    private static final List<Integer> wordIdList = new ArrayList<>();
    private static Word word = new Word();

    private static StringBuilder controlWord = new StringBuilder();
    private static StringBuilder wordToGuess = new StringBuilder();
    private static Map<Character, List<Integer>> charInWordMap = new HashMap<>();
    private static Set<Character> charGuessedSet = new HashSet<>();

    private static final Set<Character> symbolsNoSpaceAfter = WordConstant.symbolsNoSpaceAfter();
    private static final Set<Character> symbolsWithSpaceAfter = WordConstant.symbolsWithSpaceAfter();
    private static final Set<Character> vowels = WordConstant.vowel();

    public static void addNewGame() throws IOException, URISyntaxException {

        // check game name input
        while (true) {

            System.out.println();
            System.out.print("Game Name: ");
            String gameName = scan.nextLine();

            if (!gameName.trim().isEmpty()) {

                game.setGameName(gameName);
                break;
            } else {

                System.out.println("Game name cannot be blank.");
            }
        }

        System.out.println();
        System.out.print("Total Number of Team: ");
        int totalTeam;

        while (true) {
            try {
                totalTeam = Integer.parseInt(scan.nextLine());

                if (totalTeam < 2 || totalTeam > 4) { // if total team is not btw 2-4
//                    System.out.println("int");
                    System.out.println("Total number of team must be a whole number and must be between 2-4.");
                    System.out.println();
                    System.out.print("Total Number of Team: ");

                } else { // pass validation

                    game.setTotalTeam(totalTeam);
                    break;
                }
            } catch (NumberFormatException e) { // if total team is not int
//                System.out.println("not int");
                System.out.println("Total number of team must be a whole number and must be between 2-4.");
                System.out.println();
                System.out.print("Total Number of Team: ");
            }
        }

    /*
        Request
     */
        HttpPost addGameRequest = new HttpPost("http://localhost:8080/game");
        StringEntity gameToAddEntity = new StringEntity(mapper.writeValueAsString(game)
                , ContentType.APPLICATION_JSON);
        addGameRequest.setEntity(gameToAddEntity);

    /*
        Response
     */
        CloseableHttpResponse addedGameResponse = httpClient.execute(addGameRequest);

        if (addedGameResponse.getStatusLine().

                getStatusCode() != 200) {
            System.out.println("Game is not added! Status code: " + addedGameResponse.getStatusLine().getStatusCode() + " - " + addedGameResponse.getStatusLine().getReasonPhrase());
        }

        HttpEntity responseEntity = addedGameResponse.getEntity();
        game = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<Game>() {
        });

        addTeam();

    }

    public static void addTeam() throws IOException, URISyntaxException {

        int totalTeam = game.getTotalTeam();
        String teamName;
        int teamTurn;
        int gameId = game.getGameId();
        List<Integer> teamIds = new ArrayList<>();

        for (int i = 1; i < totalTeam + 1; i++) {

            // validate team name input
            while (true) {

                System.out.println();
                System.out.print("Team " + i + " Name: ");
                teamName = scan.nextLine();

                if (!teamName.trim().isEmpty()) {
                    break;
                } else {

                    System.out.println("Team name cannot be blank.");
                }
            }

            teamTurn = i; // calculate team turn

            Team teamToAdd = new Team();
            teamToAdd.setTeamName(teamName);
            teamToAdd.setTeamTurn(teamTurn);
            teamToAdd.setGameId(gameId);

            HttpPost addTeamRequest = new HttpPost("http://localhost:8080/team");
            StringEntity teamToAddEntity = new StringEntity(mapper.writeValueAsString(teamToAdd)
                    , ContentType.APPLICATION_JSON);
            addTeamRequest.setEntity(teamToAddEntity);

            CloseableHttpResponse addedTeamResponse = httpClient.execute(addTeamRequest);

            if (addedTeamResponse.getStatusLine().getStatusCode() != 200) {
                System.out.println("Round is not added! Status code: " + addedTeamResponse.getStatusLine().getStatusCode() + " - " + addedTeamResponse.getStatusLine().getReasonPhrase());
            }

            HttpEntity responseEntity = addedTeamResponse.getEntity();
            Team teamAdded = mapper.readValue(EntityUtils.toString(responseEntity)
                    , new TypeReference<Team>() {
                    });

            teamList.add(teamAdded);

            teamAdded.getTeamId();
//            System.out.println("teamAdded.getTeamId(): " + teamAdded.getTeamId());
            teamIds.add(teamAdded.getTeamId());
//            System.out.println("teamIds: " + teamIds);
        }

        addRound(teamIds);
    }

    public static void addRound(List<Integer> teamIds) throws IOException, URISyntaxException {

//        game.setGameId(3);

        AddRound roundToAdd = new AddRound();

        roundToAdd.setTeamIds(teamIds);
        roundToAdd.setGameId(game.getGameId());

        HttpPost addRoundRequest = new HttpPost("http://localhost:8080/round");
        StringEntity roundToAddEntity = new StringEntity(mapper.writeValueAsString(roundToAdd)
                , ContentType.APPLICATION_JSON);

        addRoundRequest.setEntity(roundToAddEntity);
//        System.out.println("addRoundRequest: " + EntityUtils.toString(roundToAddEntity));

        CloseableHttpResponse addedRoundResponse = httpClient.execute(addRoundRequest);

        if (addedRoundResponse.getStatusLine().getStatusCode() != 200) {
            System.out.println("Team is not added! Status code: " + addedRoundResponse.getStatusLine().getStatusCode() + " - " + addedRoundResponse.getStatusLine().getReasonPhrase());
        }
        HttpEntity responseEntity = addedRoundResponse.getEntity();
        roundList = mapper.readValue(EntityUtils.toString(responseEntity)
                , new TypeReference<>() {
                });

        setRound();
    }

    public static void setRound() throws IOException, URISyntaxException {

//        System.out.println("roundList: " + roundList);

        for (int i = 0; i < roundList.size(); i++) {

            // reset wordToGuess info
            wordToGuess.setLength(0);
            controlWord.setLength(0);
            charGuessedSet.clear();
            charInWordMap.clear();

//            System.out.println(charInWordMap.toString());
//            System.out.println(controlWord);
//            System.out.println(controlWord.length() - 1);
//            System.out.println(wordToGuess);
//            System.out.println(wordToGuess.length() - 1);

            System.out.println();
            System.out.println("=====");
            System.out.println("Round " + (i + 1) + " begin!!!");
            System.out.println("=====");

            int teamCounter = i;

            // set round
            round.setRoundId(roundList.get(i).getRoundId().getRoundId());
            round.setTeamId(roundList.get(i).getRoundId().getTeamId());
            round.setGameId(roundList.get(i).getRoundId().getGameId());

            getTeamByTeamId(round.getTeamId()); // set team according to teamId in round

            System.out.println();
            System.out.println("Contestant " + team.getTeamTurn() + " turn.");

            printContestantInformation();

            // set game
            chooseCategory();
            setWordToGuess();
            updateGameDb(); // does not update currentRound in game DB
            updateCurrentRoundInGameDB(i + 1); // update currentRound in game DB

            System.out.println(charInWordMap.toString());
            System.out.println(controlWord);
            System.out.println(controlWord.length() - 1);
            System.out.println(wordToGuess);
            System.out.println(wordToGuess.length() - 1);

            // play the game
            playTheGame(teamCounter); // play the round

            // after round finish
            updateRoundDb();
            team.setTotalScore(team.getTotalScore() + round.getRoundScore());
            updateTeamDb();

            // reset all round score in this game to 0
            resetAllRoundScoresAllSpinScores();
        }

        System.out.println();
        System.out.println("GAME OVER!");
        System.out.println("Let see who is the winner...");

        getTeamsByGameId();
        calculateWinner();
    }

    private static void resetAllRoundScoresAllSpinScores() throws URISyntaxException, IOException {

        for (int i = 0; i < roundList.size(); i++) {
            roundList.get(i).setRoundScore(0);
            roundList.get(i).setSpinScore(0);
            updateRoundDb();
        }
    }

    private static void calculateWinner() {

        String winningTeamName = null;
        int highestScore = 0;

        for (Team team : teamList) {

            System.out.println();
            System.out.println("Team " + team.getTeamName() + ": " + team.getTotalScore() + " points.");
            System.out.println("=====");

            if (team.getTotalScore() > highestScore) {
                highestScore = team.getTotalScore();
                winningTeamName = team.getTeamName();
            }
        }

        System.out.println();
        System.out.println("The winner is.... ");
        System.out.println();
        System.out.println("`¤´.¸¸.·´¨»*«| Team " + winningTeamName + " |»*«´¨`·.¸¸.`¤´");
        System.out.println();
        System.out.println("===============================");
        System.out.println(" ________         $$$$$");
        System.out.println(" |      |        $  $  $");
        System.out.println(" $      |       $   $");
        System.out.println("/|\\     |        $  $");
        System.out.println(" |      |         $$$$$");
        System.out.println("/ \\     |           $  $");
        System.out.println("        |           $   $");
        System.out.println("        |        $  $  $");
        System.out.println("     -------      $$$$$");
        System.out.println("  =====================");
        System.out.println(" |  Fortune Hang Man!  |");
        System.out.println("  =====================");
    }

    public static void getTeamsByGameId() throws IOException {

        game.setGameId(25);
        HttpGet getTeamsByGameIdRequest = new HttpGet("http://localhost:8080/team/game/" + game.getGameId());

        CloseableHttpResponse getTeamByGameIdResponse = httpClient.execute(getTeamsByGameIdRequest);
        HttpEntity responseEntity = getTeamByGameIdResponse.getEntity();

        if (getTeamByGameIdResponse.getStatusLine().getStatusCode() != 200) {
            System.out.println("Current round in game is not updated! Status code: " + getTeamByGameIdResponse.getStatusLine().getStatusCode() + " - " + getTeamByGameIdResponse.getStatusLine().getReasonPhrase());
        }

        teamList = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<List<Team>>() {
        });

        for (Team team : teamList) {
            System.out.println(team.toString());
        }
    }

    public static void updateCurrentRoundInGameDB(int currentRound) throws IOException, URISyntaxException {

//        game.setGameId(2);

        URIBuilder builder = new URIBuilder("http://localhost:8080/game/" + game.getGameId());

        if (game.getCurrentRound() != currentRound) {
//            System.out.println("updatingGame currentRound: currentRound = " + currentRound);
            builder.setParameter("currentRound", Integer.toString(currentRound));
        }

        HttpPut updateGameRequest = new HttpPut(builder.build());
        StringEntity updateGameEntity = new StringEntity(mapper.writeValueAsString(updateGameRequest), ContentType.APPLICATION_JSON);
        updateGameRequest.setEntity(updateGameEntity);

//        System.out.println(updateGameRequest.toString());

        CloseableHttpResponse updateGameResponse = httpClient.execute(updateGameRequest);

        if (updateGameResponse.getStatusLine().getStatusCode() != 200) {
            System.out.println("Current round in game is not updated! Status code: " + updateGameResponse.getStatusLine().getStatusCode() + " - " + updateGameResponse.getStatusLine().getReasonPhrase());
        }

        HttpEntity responseEntity = updateGameResponse.getEntity();
        game = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<Game>() {
        });

//        System.out.println("Game: " + game.toString());
    }

    public static void chooseCategory() throws IOException, URISyntaxException {
        HttpGet getCategoriesRequest = new HttpGet("http://localhost:8080/category");

        CloseableHttpResponse getCategoriesResponse = httpClient.execute(getCategoriesRequest);
        HttpEntity getCategoriesEntity = getCategoriesResponse.getEntity();

        if (getCategoriesResponse.getStatusLine().getStatusCode() != 200) {
            System.out.println("Cannot get categories! Status code: " + getCategoriesResponse.getStatusLine().getStatusCode() + " - " + getCategoriesResponse.getStatusLine().getReasonPhrase());
        }

        List<String> categories = mapper.readValue(EntityUtils.toString(getCategoriesEntity), new TypeReference<>() {
        });
        StringBuilder categoriesList = new StringBuilder();

        for (int i = 0; i < categories.size(); i++) {
            if (i != categories.size() - 1) {
                categoriesList.append(categories.get(i) + ", ");
            } else {
                categoriesList.append(categories.get(i));
            }
        }

        // choose category
        System.out.println();
        System.out.println("=====");
        System.out.println("Choose one of these categories: " + categoriesList);
        System.out.print("Category: ");
        String categoryName;

        while (true) {
            try {
                categoryName = scan.nextLine().toUpperCase();

                if (!categories.contains(categoryName.toUpperCase())) {
                    System.out.println();
                    System.out.println("Category chosen is not in the list. Please type one of these categories: " + categoriesList);
                    System.out.print("Category: ");
                } else {
                    categoryName = categoryName.replaceAll(" ", "%20");

                    // get random word
                    getRandomWord(categoryName);
//                    System.out.println("word before: " + word);
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Category chosen is not in the list. Please type one of these categories:" + categoriesList);
                System.out.print("Category: ");
            }
        }

        if (wordIdList.contains(word.getWordId())) {
            getRandomWord(categoryName);
        } else {
            wordIdList.add(word.getWordId());
        }

//        System.out.println("word: " + word);
//        System.out.println("wordIdList: " + wordIdList);
    }

    private static void getRandomWord(String categoryName) throws IOException, URISyntaxException {
        HttpGet getRandomWordRequest = new HttpGet("http://localhost:8080/randWord/" + categoryName);

        CloseableHttpResponse getRandomWordReqponse = httpClient.execute(getRandomWordRequest);
        HttpEntity getRandomWordEntity = getRandomWordReqponse.getEntity();

        if (getRandomWordReqponse.getStatusLine().getStatusCode() != 200) {
            System.out.println("Cannot get random word! Status code: " + getRandomWordReqponse.getStatusLine().getStatusCode() + " - " + getRandomWordReqponse.getStatusLine().getReasonPhrase());
        }

        word = mapper.readValue(EntityUtils.toString(getRandomWordEntity), new TypeReference<Word>() {
        });

        updateWordInGameDb();
    }

    public static void updateWordInGameDb() throws URISyntaxException, IOException {

//        game.setGameId(2);
//        word.setWordId(1);

        URIBuilder builder = new URIBuilder("http://localhost:8080/game/" + game.getGameId());

        if (game.getWord() == null || (word.getWordId() != game.getWord().getWordId())) {
//            System.out.println("updatingGame Word: wordId = " + word.getWordId());
            builder.setParameter("wordId", Integer.toString(word.getWordId()));
            game.setWord(word);
        }

        HttpPut updateGameRequest = new HttpPut(builder.build());
        StringEntity updateGameEntity = new StringEntity(mapper.writeValueAsString(updateGameRequest), ContentType.APPLICATION_JSON);
        updateGameRequest.setEntity(updateGameEntity);

//        System.out.println(updateGameRequest.toString());

        CloseableHttpResponse updateGameResponse = httpClient.execute(updateGameRequest);

        if (updateGameResponse.getStatusLine().getStatusCode() != 200) {
            System.out.println("Word in game is not updated! Status code: " + updateGameResponse.getStatusLine().getStatusCode() + " - " + updateGameResponse.getStatusLine().getReasonPhrase());
        }

        HttpEntity responseEntity = updateGameResponse.getEntity();
        game = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<Game>() {
        });

//        System.out.println("Game: " + game.toString());
    }

    public static void updateGameDb() throws URISyntaxException, IOException {

//        game.setGameId(1);
//        round.setRoundId(4);
//        game.setRoundId(3);

        URIBuilder builder = new URIBuilder("http://localhost:8080/game/" + game.getGameId());

        if (round.getRoundId() != game.getRoundId()) {
//            System.out.println("updatingGame Round: roundId = " + round.getRoundId() + " gameId = " + game.getRoundId());
            builder.setParameter("roundId", Long.toString(round.getRoundId()));
            game.setRoundId(round.getRoundId());
        }

        if (game.getGameStatus() == null) {
//            System.out.println("updating game status");
            builder.addParameter("gameStatus", "in progress");
            game.setGameStatus("IN PROGRESS");
        }

        if (team.getTeamTurn() != game.getCurrentTeamTurn()) {
//            System.out.println("updatingGame currentTeamTurn: teamTeamTurn = " + team.getTeamTurn() + " game.currentTeamTurn = " + game.getCurrentTeamTurn());
            builder.addParameter("currentTeamTurn", Integer.toString(team.getTeamTurn()));
            game.setCurrentTeamTurn(team.getTeamTurn());
        }

        HttpPut updateGameRequest = new HttpPut(builder.build());
        StringEntity updateGameEntity = new StringEntity(mapper.writeValueAsString(updateGameRequest), ContentType.APPLICATION_JSON);
        updateGameRequest.setEntity(updateGameEntity);

//        System.out.println(updateGameRequest.toString());

        CloseableHttpResponse updateGameResponse = httpClient.execute(updateGameRequest);

        if (updateGameResponse.getStatusLine().getStatusCode() != 200) {
            System.out.println("Game is not updated! Status code: " + updateGameResponse.getStatusLine().getStatusCode() + " - " + updateGameResponse.getStatusLine().getReasonPhrase());
        }

        HttpEntity responseEntity = updateGameResponse.getEntity();
        game = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<Game>() {
        });

//        System.out.println("Game: " + game.toString());
    }

    private static void updateCharGuessedInGame(char charGuessed) throws URISyntaxException, IOException {

        URIBuilder builder = new URIBuilder("http://localhost:8080/game/" + game.getGameId());

        String charGuessedString = Character.toString(charGuessed);
        builder.addParameter("letterGuessed", charGuessedString);
        game.setLetterGuessed(charGuessedString);

        HttpPut updateGameRequest = new HttpPut(builder.build());
        StringEntity updateGameEntity = new StringEntity(mapper.writeValueAsString(updateGameRequest), ContentType.APPLICATION_JSON);
        updateGameRequest.setEntity(updateGameEntity);

//        System.out.println(updateGameRequest);

        CloseableHttpResponse updateGameResponse = httpClient.execute(updateGameRequest);

        if (updateGameResponse.getStatusLine().getStatusCode() != 200) {
            System.out.println("Character guessed in game is not updated! Status code: " + updateGameResponse.getStatusLine().getStatusCode() + " - " + updateGameResponse.getStatusLine().getReasonPhrase());
        }

        HttpEntity responseEntity = updateGameResponse.getEntity();
        game = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<Game>() {
        });

        //        System.out.println("Game: " + game.toString());
    }

    private static void updateTeamDb() throws URISyntaxException, IOException {

        URIBuilder builder = new URIBuilder("http://localhost:8080/team/" + team.getTeamId());
        builder.addParameter("totalScore", Integer.toString(team.getTotalScore()));

        HttpPut updateGameRequest = new HttpPut(builder.build());
        StringEntity updateGameEntity = new StringEntity(mapper.writeValueAsString(updateGameRequest), ContentType.APPLICATION_JSON);
        updateGameRequest.setEntity(updateGameEntity);

//        System.out.println(updateGameRequest.toString());

        CloseableHttpResponse updateTeamResponse = httpClient.execute(updateGameRequest);

        if (updateTeamResponse.getStatusLine().getStatusCode() != 200) {
            System.out.println("Team's total score is not updated! Status code: " + updateTeamResponse.getStatusLine().getStatusCode() + " - " + updateTeamResponse.getStatusLine().getReasonPhrase());
        }

        HttpEntity responseEntity = updateTeamResponse.getEntity();
        team = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<Team>() {
        });

        //        System.out.println("Tame: " + team.toString());
    }

    private static void getTeamByTeamId(int teamId) throws IOException {
        HttpGet getTeamByTeamIdRequest = new HttpGet("http://localhost:8080/team/" + teamId);

        CloseableHttpResponse getTeamByTeamIdResponse = httpClient.execute(getTeamByTeamIdRequest);
        HttpEntity responseEntity = getTeamByTeamIdResponse.getEntity();

        if (getTeamByTeamIdResponse.getStatusLine().getStatusCode() != 200) {
            System.out.println("Cannot get team by team ID of " + teamId + "! Status code: " + getTeamByTeamIdResponse.getStatusLine().getStatusCode() + " - " + getTeamByTeamIdResponse.getStatusLine().getReasonPhrase());
        }

        team = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<Team>() {
        });

        //System.out.println("Team: " + team.toString());
    }

    public static void updateRoundDb() throws URISyntaxException, IOException {

//        round.setRoundId(31);
//        round.setSpinToken(true);
//        team.setTeamId(13);

//        System.out.println("TEAM ID: "+ team.getTeamId());
        URIBuilder builder = new URIBuilder("http://localhost:8080/round/" + round.getRoundId() + "/" + team.getTeamId());

//        System.out.println("updateRoundDb - roundScore: " + round.getRoundScore());
        builder.addParameter("roundScore", Integer.toString(round.getRoundScore()));

//        System.out.println("updateRoundDb - spinScore: " + round.getSpinScore());
        builder.addParameter("spinScore", Integer.toString(round.getSpinScore()));

//        System.out.println("updateRoundDb - spintToken: " + round.isSpinToken());
        builder.addParameter("spinToken", Boolean.toString(round.isSpinToken()));

        HttpPut updateRoundRequest = new HttpPut(builder.build());
        StringEntity updateGameEntity = new StringEntity(mapper.writeValueAsString(updateRoundRequest), ContentType.APPLICATION_JSON);
        updateRoundRequest.setEntity(updateGameEntity);

//        System.out.println(updateRoundRequest.toString());

        CloseableHttpResponse updateRoundResponse = httpClient.execute(updateRoundRequest);

        if (updateRoundResponse.getStatusLine().getStatusCode() != 200) {
            System.out.println("Round is not updated! Status code: " + updateRoundResponse.getStatusLine().getStatusCode() + " - " + updateRoundResponse.getStatusLine().getReasonPhrase());
        }

        HttpEntity responseEntity = updateRoundResponse.getEntity();
        RoundResponse roundResponse = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<RoundResponse>() {
        });

//        System.out.println("RoundResponse: " + roundResponse.toString());
    }

    public static void getRoundByRoundIdTeamId() throws IOException {

//        round.setRoundId(18);
//        team.setTeamId(9);

        HttpGet getRoundByRoundIdTeamIdRequest = new HttpGet("http://localhost:8080/roundTeam/" + round.getRoundId() + "/" + team.getTeamId());

        CloseableHttpResponse getRoundByRoundIdTeamIdResponse = httpClient.execute(getRoundByRoundIdTeamIdRequest);
        HttpEntity responseEntity = getRoundByRoundIdTeamIdResponse.getEntity();

//        System.out.println("getRoundByRoundIdTeamId responseEntity: " + EntityUtils.toString(responseEntity));

        if (getRoundByRoundIdTeamIdResponse.getStatusLine().getStatusCode() != 200) {
            System.out.println("Cannot get round with round ID of " + round.getRoundId() + ", team ID of " + team.getTeamId() + " Status code: " + getRoundByRoundIdTeamIdResponse.getStatusLine().getStatusCode() + " - " + getRoundByRoundIdTeamIdResponse.getStatusLine().getReasonPhrase());
        }

        RoundResponse roundResponse = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<RoundResponse>() {
        });

        round.setRoundId(RoundResponse.getRoundId().getRoundId());
        round.setTeamId(RoundResponse.getRoundId().getTeamId());
        round.setRoundScore(RoundResponse.getRoundScore());
        round.setSpinToken(RoundResponse.isSpinToken());

//        System.out.println("RoundResponse: " + roundResponse.toString());
    }

    public static void playTheGame(int teamCounter) throws URISyntaxException, IOException {

//        game.setGameId(10);
//        round.setRoundId(26);
//        round.setTeamId(17);
//        String w = "Banana";
//        word.setWord(w.toUpperCase());

        while (controlWord.compareTo(wordToGuess) != 00) {

            spinTheWheel(teamCounter);
            charInputManagement(teamCounter);
        }

//        while (true) {
//
//            spinTheWheel(teamCounter); // spin the wheel
//
//            System.out.println();
//            System.out.println(wordToGuess);
//
//            System.out.print("Guess a character: ");
//            String guessedInput = isCharGuessedEmpty().trim().toUpperCase();
//
//            if (guessedInput.length() > 1) { // if input more than 1 character or guessed the word
//
//                if (word.getWord().equals(guessedInput)) { // if guess the word correct, move on to next round
//
//                    guessedTheWordCorrect();
//                    break;
//                } else { // guessed the word wrong, change to next team
//                    // change team
//                    System.out.println();
//                    System.out.println("You guessed wrong! Next team turn to guess.");
//                    System.out.println();
//                    System.out.println("Your team information:");
//                    nextTeamFunction(teamCounter); // switch to next team
//                }
//            } else {
//                char charGuessed = guessedInput.charAt(0);
//
//                checkIfVowel(charGuessed, teamCounter);
//                guessChar(charGuessed, teamCounter);
//
////            System.out.println("controlWord: " + controlWord);
////            System.out.println("wordToGuess: " + wordToGuess);
//                if (controlWord.compareTo(wordToGuess) == 0) {
//
//                    System.out.println();
//                    System.out.println("*****");
//                    System.out.println("You finish the word!!!");
//                    System.out.println("*****");
//                    break;
//                }
//            }
//
////            System.out.println("here before setSpintScore to 0");
//
//            // reset spin score after every guess
//            round.setSpinScore(0);
//            updateRoundDb();
//        }

        System.out.println();
        System.out.println(wordToGuess);
        System.out.println("*****");
        System.out.println("You guessed correct!!!");
        System.out.println("*****");

        System.out.println();
//        System.out.println("You got the " + round.getSpinScore() + " point your spun on.");
        System.out.println("Total Score: " + team.getTotalScore());

        round.setSpinScore(0);
        updateRoundDb();
    }

    private static void charInputManagement(int teamCounter) throws URISyntaxException, IOException {
        //A teams turn loop
        do{
            System.out.println();
            System.out.println(wordToGuess);

            System.out.print("Guess a character: ");
            String guessedCharInput = scan.nextLine().trim().toUpperCase();

        }while(charInputValidation);


        /*  Validate if...
            1. input is more than 1 letter -> guess a word
            2. input is a vowel -> calculate point to buy vowel
            3. input is a number or is blank -> error message
         */
        charInputValidation(teamCounter, guessedCharInput);

        /*  Validate if
            1. guess char that is already guessed
            2. guess new char
         */
        calculateCharGuessed(teamCounter, guessedCharInput);
    }

    private static Boolean charInputValidation(int teamCounter, String guessedCharInput) throws URISyntaxException, IOException {

        try {

            char charGuessed = guessedCharInput.charAt(0);

            /*  Validate if...
                1. input is more than 1 letter -> guess a word
                2. input is a vowel -> calculate point to buy vowel
                3. input is a number or is blank -> error message
             */
            if (guessedCharInput.length() > 1) { // if input more than 1 character or guessed the word

                if (word.getWord().equals(guessedCharInput)) { // if guess the word correct, move on to next round

                    // set roundScore based on spinScore
                    round.setRoundScore(round.getRoundScore() + round.getSpinScore());
                    updateRoundDb();

                    // set totalScore based on roundScore
                    team.setTotalScore(team.getTotalScore() + round.getRoundScore());
                    updateTeamDb();

                    // break playTheGame() loop
                    wordToGuess = controlWord;
                } else { // guessed the word wrong

                    if (round.isSpinToken() == true) { // have token

                        System.out.println();
                        System.out.println("Congratulation... on guessing wrong!");
                        System.out.println("You are so lucky, you have a token! You can continue playing.");

                        round.setSpinToken(false);
                        round.setSpinScore(0);
                        updateRoundDb();

                        playTheGame(teamCounter);

                    } else { // no token
                        // change team
                        System.out.println();
                        System.out.println("Too bad, you guessed wrong! Next team turn");
                        System.out.println();
                        System.out.println("Your information:");
                        printContestantInformation();

                        nextTeamFunction(teamCounter); // switch to next team
                    }
                }
            } else if (vowels.contains(charGuessed)) {

                System.out.println();
                System.out.println("$$$ A vowel cost 250 points. $$$");
                System.out.println("You have " + round.getRoundScore() + " points.");

                if (round.getRoundScore() < 250) { // don't have enough point to buy vowel
                    System.out.println();
                    System.out.println("uhh...");
                    System.out.println("Looks like you do not have enough point to buy a vowel.");
                    System.out.println("Please guess another character.");

                    guessedCharInput = ""; // empty the string input
                    charGuessed = '\0'; // empty char input
                    charInputManagement(teamCounter); // guess again

                } else { // have enough point to buy vowel

                    round.setRoundScore(round.getRoundScore() - 250);
                    System.out.println("You bought a vowel. You now have " + round.getRoundScore() + " point.");

                    calculateCharGuessed(teamCounter, guessedCharInput); // bought a vowel
                }
            } else if (guessedCharInput.trim().isEmpty() || !guessedCharInput.trim().matches("^[\\sA-Z]+$")) { // blank input
                System.out.println();
                System.out.println("You must guess a letter.");

                guessedCharInput = ""; // empty the string input
                charGuessed = '\0'; // empty char input
                charInputManagement(teamCounter); // guess again
            }
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println();
            System.out.println("You must guess a letter.");

            guessedCharInput = ""; // empty the string input
            charInputManagement(teamCounter); // guess again
        }
    }

    private static void calculateCharGuessed(int teamCounter, String guessedCharInput) throws URISyntaxException, IOException {

        try {
            char charGuessed = guessedCharInput.charAt(0);

            System.out.println("calculateCharGuessed - charGuessed: " + charGuessed);

            updateCharGuessedInGame(charGuessed); // update character guessed in db

        /*  Validate if
            1. guess char that is already guessed
            2. guess new char
         */
            if (charGuessedSet.contains(guessedCharInput)) { // if char guess is already guessed

                if (round.isSpinToken() == true) { // have token

                    System.out.println();
                    System.out.println(guessedCharInput + " has been guessed already....");
                    System.out.println("How fortunate, you have a TOKEN. You can continue guessing.");

                    guessedCharInput = ""; // empty the string input
                    charGuessed = '\0'; // empty char input

                    round.setSpinToken(false);
                    round.setSpinScore(0);
                    updateRoundDb();

                    charInputManagement(teamCounter);

                } else {

                    System.out.println();
                    System.out.println(guessedCharInput + " has been guessed already... You lose your turn here.");
                    System.out.println();
                    System.out.println("Your information:");
                    printContestantInformation();

                    guessedCharInput = ""; // empty the string input
                    charGuessed = '\0'; // empty char input

                    round.setSpinScore(0);
                    updateRoundDb();

                    nextTeamFunction(teamCounter); // switch to next team
                }
            } else { // guessed new letter

                charGuessedSet.add(charGuessed); // add the letter to charGuessedSet

                if (charInWordMap.containsKey(charGuessed)) { // guess character correct

                    System.out.println("calculateCharGuessed - guess correct");

                    // calculate scoreWon (charNumber * spinScore)
                    List<Integer> charGuessedList = charInWordMap.get(charGuessed);
                    int charNumber = charGuessedList.size();
                    int scoreWon = charNumber * round.getSpinScore();

                    for (int i = 0; i < charGuessedList.size(); i++) {

                        System.out.println("calculateCharGuessed - set wordToGuess after guess");
                        wordToGuess.setCharAt(charGuessedList.get(i), charGuessed);
                        System.out.println("wordToGuess: " + wordToGuess);
                    }

                    // update roundScore and reset spinScore
                    round.setRoundScore(round.getRoundScore() + scoreWon);
                    round.setSpinScore(0);
                    updateRoundDb();

//                System.out.println();
//                System.out.println(wordToGuess);
//                System.out.println("Round score: " + round.getRoundScore());

                } else { // guess character wrong

                    if (round.isSpinToken() == true) { // have token

                        System.out.println();
                        System.out.println("You guessed wrong...");
                        System.out.println("Lucky, you have a TOKEN! You can continue guessing.");

                        guessedCharInput = ""; // empty the string input
                        charGuessed = '\0'; // empty char input

                        round.setSpinToken(false);
                        round.setSpinScore(0);
                        updateRoundDb();

                        charInputManagement(teamCounter);

                    } else { // don't have token
                        // change team
                        System.out.println();
                        System.out.println("Too bad! You guessed wrong! Next team turn.");
                        System.out.println();
                        System.out.println("Your information:");
                        printContestantInformation();

                        guessedCharInput = ""; // empty the string input
                        charGuessed = '\0'; // empty char input

                        round.setSpinScore(0);
                        updateRoundDb();

                        nextTeamFunction(teamCounter); // switch to next team
                    }
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println();
            System.out.println("You must guess a letter.");
            charInputManagement(teamCounter); // guess again
        }
    }

//    private static String isCharGuessedEmpty() {
//
//        while (true) {
//            String input = scan.nextLine();
//
//            if (!input.trim().isEmpty()) {
//
////                System.out.println("isCharGuessedEmpty input: " + input);
//                return input;
//            } else {
//                System.out.println("You must guess a character");
//            }
//        }
//    }

//    private static void guessedTheWordCorrect() throws URISyntaxException, IOException {
//
//        round.setRoundScore(round.getRoundScore() + round.getSpinScore());
//        updateRoundDb();
//        team.setTotalScore(team.getTotalScore() + round.getRoundScore());
//        updateTeamDb();
//
//        System.out.println();
//        System.out.println(controlWord);
//        System.out.println("*****");
//        System.out.println("You guessed correctly!!!");
//        System.out.println("*****");
//
//        System.out.println();
//        System.out.println("You got the " + round.getSpinScore() + " point you spin on.");
//        System.out.println("Team Score: " + team.getTotalScore());
//    }

//    private static void checkIfVowel(char charGuessed, int teamCounter) throws URISyntaxException, IOException {
//
//        if (vowels.contains(charGuessed)) {
//
//            System.out.println();
//            System.out.println("$$$ A vowel cost 250 point. $$$");
//            System.out.println("You have " + round.getRoundScore() + " point.");
//
//            if (round.getRoundScore() < 250) { // if don't have enough point to buy vowel
//
//                System.out.println();
//                System.out.println("uhh...");
//                System.out.println("Looks like you do not have enough point to buy a vowel.");
//                System.out.println("Please guess another character.");
//
//                System.out.println();
//                System.out.println(wordToGuess);
//
//                System.out.print("Guess a character: ");
//                String guessedInput = isCharGuessedEmpty().trim().toUpperCase();
//                char newCharGuessed = guessedInput.charAt(0);
//
//                guessChar(newCharGuessed, teamCounter);
//
//            } else { // if have enough point to buy vowel
//                round.setRoundScore(round.getRoundScore() - 250);
//                System.out.println("You bought a vowel. You now have " + round.getRoundScore() + " point.");
//
//                guessChar(charGuessed, teamCounter);
//
////                 // can't print here... wordToGuess is not updated yet
////                System.out.println();
////                System.out.println(wordToGuess);
//            }
//        }
//    }

//    private static void guessChar(char charGuessed, int teamCounter) throws URISyntaxException, IOException {
//
//        updateCharGuessedInGame(charGuessed); // update character guessed in db
//
//        if (charGuessedSet.contains(charGuessed)) { // if char guess is already guessed
//
//            if (round.isSpinToken() == true) {
//
//                System.out.println();
//                System.out.println(charGuessed + " is already guessed....");
//                System.out.println("But you have a token! You can continue guessing.");
//
//                round.setSpinToken(false);
//                updateRoundDb();
//
//                spinTheWheel(teamCounter); // spin the wheel
//
//                System.out.println();
//                System.out.println(wordToGuess);
//
//                System.out.print("Guess a character: ");
//                String guessedInput = isCharGuessedEmpty().trim().toUpperCase();
//                char newCharGuessed = guessedInput.charAt(0);
//
//                guessChar(newCharGuessed, teamCounter);
//
//            } else {
//
//                System.out.println();
//                System.out.println(charGuessed + " is already guessed. Next team turn to guess.");
//                System.out.println("Your team information:");
//                nextTeamFunction(teamCounter); // switch to next team
//            }
//
//        } else { // if new character guess
//
//            charGuessedSet.add(charGuessed);
//
//            if (charInWordMap.containsKey(charGuessed)) { // if guess a character correctly
//
//                List<Integer> charGuessedList = charInWordMap.get(charGuessed);
//                int charNumber = charGuessedList.size();
//                int scoreWon = charNumber * round.getSpinScore();
//
//                for (int i = 0; i < charGuessedList.size(); i++) {
//
//                    wordToGuess.setCharAt(charGuessedList.get(i), charGuessed);
//                }
//
//                round.setRoundScore(round.getRoundScore() + scoreWon);
//                updateRoundDb();
//
//                System.out.println();
//                System.out.println(wordToGuess);
//                System.out.println("Round score: " + round.getRoundScore());
//
//            } else { // if character is not in the word
//
//                if (round.isSpinToken() == true) { // if guessed wrong and have token
//
//                    System.out.println();
//                    System.out.println("You guessed wrong...");
//                    System.out.println("But you have a token! You can continue guessing.");
//
//                    round.setSpinToken(false);
//                    updateRoundDb();
//
//                    spinTheWheel(teamCounter); // spin the wheel
//
//                    System.out.println();
//                    System.out.println(wordToGuess);
//
//                    System.out.print("Guess a character: ");
//                    String guessedInput = isCharGuessedEmpty().trim().toUpperCase();
//                    char newCharGuessed = guessedInput.charAt(0);
//
//                    guessChar(newCharGuessed, teamCounter);
//
//                } else { // if guessed wrong and dont have token
//                    // change team
//                    System.out.println();
//                    System.out.println("You guessed wrong! Next team turn to guess.");
//                    System.out.println();
//                    System.out.println("Your team information:");
//                    nextTeamFunction(teamCounter); // switch to next team
//                }
//            }
//        }
//    }

    private static void printContestantInformation() {
        System.out.println("=====");
        System.out.println("Name: " + team.getTeamName());
        System.out.println("Total Score: " + team.getTotalScore());
        System.out.println("Round Score: " + round.getRoundScore());
        System.out.println("Token: " + round.isSpinToken());
        System.out.println("=====");
    }

    private static void nextTeamFunction(int teamCounter) throws URISyntaxException, IOException {

        updateTeamDb(); // update current team total score in DB

        round.setSpinScore(0);
        updateRoundDb(); // update current round with current team in DB

//        System.out.println("teamList.size(): " + teamList.size());

        // get next team using teamCounter
        if (teamCounter + 1 < teamList.size()) {
            teamCounter++; // next team in the list turn
//            System.out.println("incrementing teamCounter: " + teamCounter);

        } else {
            teamCounter = 0; // start at 0 index if next team turn is bigger than the teamList size
//            System.out.println("resetting: " + teamCounter);
        }

        // change team instance to next team by getting next team info from db
//        System.out.println("teamCounter: " + teamCounter);
        getTeamByTeamId(teamList.get(teamCounter).getTeamId());

        System.out.println();
        System.out.println("Next contestant information: ");
        printContestantInformation();

        // change round instance based on roundId teamID
        getRoundByRoundIdTeamId();

        // update game DB
        updateGameDb();

        playTheGame(teamCounter); // play the game
    }

    public static void setWordToGuess() {

        String wordChosen = word.getWord();

//        game.getWord().setWord(w.toUpperCase());
//        String wordChosen = game.getWord().getWord();

        /*
            setting spaces on in the wordChosen
         */
        for (int i = 0; i < wordChosen.length(); i++) {

            char currentChar = wordChosen.charAt(i);

            if (symbolsWithSpaceAfter.contains(currentChar)) { // if symbols allows space after
                controlWord.append(currentChar);

            } else if (symbolsNoSpaceAfter.contains(currentChar)) { // if symbols don't allow space after then check if char after is alphabet
                controlWord.append(currentChar);

                // if char after ' or - is an alphabet and if i < wordChosen length, add space after
                if ((i + 1) < wordChosen.length() && wordChosen.charAt(i + 1) >= 'A' && wordChosen.charAt(i + 1) <= 'Z') {
                    controlWord.append(wordChosen.charAt(i + 1));
                    i++;
                }
            } else if (i == 0) {
                controlWord.append(wordChosen.charAt(0));
            } else if (wordChosen.charAt(i) == ' ') {
                controlWord.append("  ");
            } else {
                controlWord.append(" " + currentChar);
            }
        }

        /*
            setting word to guess in the game
         */
        for (int i = 0; i < controlWord.length(); i++) {
            char currentChar = controlWord.charAt(i);

            if (!symbolsNoSpaceAfter.contains(currentChar) && !symbolsWithSpaceAfter.contains(currentChar) && currentChar != ' ') {
                if (!charInWordMap.containsKey(currentChar)) {
                    charInWordMap.put(currentChar, new ArrayList<>()); // instantiate arrayList at the first char
                    charInWordMap.get(currentChar).add(i); // add the first index to the value of map
                } else {
                    charInWordMap.get(currentChar).add(i);
                }

            } else if ((currentChar == '\'' || currentChar == '-') && (i + 1) < controlWord.length()) { // add character after ' because it will get skip
                char nextChar = controlWord.charAt(i + 1);

                if (!charInWordMap.containsKey(nextChar)) {
                    charInWordMap.put(nextChar, new ArrayList<>()); // instantiate arrayList at the first char
                    charInWordMap.get(nextChar).add(i + 1); // add the first index to the value of map
                } else {
                    charInWordMap.get(nextChar).add(i + 1);
                }
            }

            if (symbolsWithSpaceAfter.contains(currentChar)) { // if symbols allows space after
                wordToGuess.append(currentChar);
            } else if (symbolsNoSpaceAfter.contains(currentChar)) { // if symbols dont allows space after if next char is an alphabet
                wordToGuess.append(currentChar);

                // if char after ' or - is an alphabet and i + 1 must be less than the length of the controlWord, then add space after
                if ((i + 1) < controlWord.length() && controlWord.charAt(i + 1) >= 'A' && controlWord.charAt(i + 1) <= 'Z') {
                    wordToGuess.append("_");
                    i++;
                }
            } else if (controlWord.charAt(i) == ' ') {
                wordToGuess.append(" ");
            } else {
                wordToGuess.append("_");
            }
        }

//        System.out.println(charInWordMap.toString());
//        System.out.println(controlWord);
//        System.out.println(controlWord.length() - 1);
//        System.out.println(wordToGuess);
//        System.out.println(wordToGuess.length() - 1);
    }

    public static void spinTheWheel(int teamCounter) throws IOException {

//        round.setRoundId(79);
//        team.setTeamId(74);
//        getRoundByRoundIdTeamId();

        // show word before spinning the wheel
        System.out.println();
        System.out.println(wordToGuess);
        System.out.println("Round score: " + round.getRoundScore());

        int spinScore;

        while (true) {
            try {

                // spin the wheel
                System.out.println();
                System.out.println("=====");
                System.out.println("Spin the wheel.");
                System.out.print("Let us know what you get: ");

                String spinWheel = scan.nextLine();

//                System.out.println("spinWheel: " + spinWheel.toUpperCase().trim());

                if (spinWheel.toUpperCase().trim().equals("TOKEN")) { // landed on TOKEN
                    round.setSpinToken(true);
                    updateRoundDb();

                    System.out.println();
                    System.out.println("You got a TOKEN! Congratulation!");
                    System.out.println("This token will be used when you guess wrong, you guess a character that has already been guessed, or went bankrupt.");
                    break;

                } else if (spinWheel.toUpperCase().trim().equals("SKIP")) { // landed on SKIP

                    if (round.isSpinToken() == true) { // have token

                        System.out.println();
                        System.out.println("You landed on SKIP, which means your turn end here.");
                        System.out.println("However, you have a token! You can continue playing.");

                        round.setSpinToken(false);
                        updateRoundDb();

                        playTheGame(teamCounter);

                    } else { // dont have token, switch turn

                        System.out.println();
                        System.out.println("How unfortunate... your turn ended here. Better luck next time.");
                        System.out.println("Your information:");
                        printContestantInformation();

                        round.setSpinScore(0);
                        updateRoundDb();

                        nextTeamFunction(teamCounter); // switch to next team
                        break;
                    }
                } else if (spinWheel.toUpperCase().trim().equals("BANKRUPT")) { // landed on BANKRUPTCY

                    if (round.isSpinToken() == true) { // have token

                        System.out.println();
                        System.out.println("Looks like you went BANKRUPT...  You loose all your money from this round and your turn end here.");
                        System.out.println("However, you have a TOKEN! You can continue playing.");

                        round.setSpinToken(false);
                        updateRoundDb();

                        playTheGame(teamCounter);

                    } else {
                        System.out.println();
                        System.out.println("You spend too much money you went BANKRUPT. You lose all your money from this round. You are too broke to continue playing.");
                        System.out.println("Your information:");

                        round.setSpinScore(0);
                        round.setRoundScore(0);
                        updateRoundDb();

                        printContestantInformation();

                        nextTeamFunction(teamCounter); // switch to next team
                        break;
                    }
                } else {

                    spinScore = Integer.parseInt(spinWheel);
                    round.setSpinScore(spinScore);
                    updateRoundDb();
                    break;
                }
            } catch (NumberFormatException | URISyntaxException e) {

                System.out.println();
                System.out.println("Incorrect answer...");
                System.out.println("Enter the amount as a whole number, unless it is a 'token', 'bankrupt', or 'skip'.");
            }
        }
    }
}
