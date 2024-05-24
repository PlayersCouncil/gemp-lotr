import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.game.LotroCardBlueprint;
import org.hjson.JsonValue;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.Set;

public class ValidateCards {
    public static void main(String[] args) {
        LotroCardBlueprintBuilder cardBlueprintBuilder = new LotroCardBlueprintBuilder();
        final String property = System.getProperty("user.dir");
        String projectRoot = new File(property).getAbsolutePath();

        File path = new File(projectRoot + "/gemp-lotr-cards/src/main/resources/cards");

        processPath(cardBlueprintBuilder, path);
    }

    private static void processPath(LotroCardBlueprintBuilder cardBlueprintBuilder, File path) {
        for (File file : path.listFiles()) {
            if (file.isFile()) {
                loadCardsFromFile(cardBlueprintBuilder, file);
                System.out.println("Finished loading file: " + file.getName());
            } else {
                processPath(cardBlueprintBuilder, file);
            }
        }
    }

    private static void loadCardsFromFile(LotroCardBlueprintBuilder cardBlueprintBuilder, File file) {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(file)) {
            String json = JsonValue.readHjson(reader).toString();
            final JSONObject cardsFile = (JSONObject) parser.parse(json);
            final Set<Map.Entry<String, JSONObject>> cardsInFile = cardsFile.entrySet();
            for (Map.Entry<String, JSONObject> cardEntry : cardsInFile) {
                String blueprint = cardEntry.getKey();
                final JSONObject cardDefinition = cardEntry.getValue();
                try {
                    final LotroCardBlueprint lotroCardBlueprint = cardBlueprintBuilder.buildFromJson(blueprint, cardDefinition);
                } catch (InvalidCardDefinitionException exp) {
                    System.out.println("Unable to load card " + blueprint);
                    exp.printStackTrace();
                }
            }
        } catch (Exception exp) {
            System.out.println("Unable to load set from file: " + file.getName());
            exp.printStackTrace();
        }
    }
}
