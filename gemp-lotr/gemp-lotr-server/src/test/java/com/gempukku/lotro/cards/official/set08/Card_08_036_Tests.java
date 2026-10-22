package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("garrison", "8_36");
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion; Knight is its whole text
					// Knight of Gondor: [gondor] Man companion, no requirement to play; a fellowship ability used only
					// on purpose
					put("kog", "5_36");
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a skirmish ability used only
					// on purpose
					put("pippin", "103_31");
					// Gondorian Merchant: the [gondor] ally, a [gondor] character who is no companion; his maneuver
					// ability is used only on purpose
					put("merchant", "7_97");
					// Rider of Rohan: a Man companion of another culture; his skirmish ability needs him mounted
					put("rider", "4_286");

					// First Level: a [gondor] fortification condition, cost 1; a maneuver ability used only on purpose
					put("firstlevel", "7_92");
					// Stronghold of My Fathers: a [rohan] fortification POSSESSION in the support area; its response
					// answers a wound on a [rohan] companion, and none is taken here
					put("stronghold", "102_48");
					// Foes of Mordor: a [gondor] condition in the support area that is no fortification; a maneuver
					// ability used only on purpose
					put("foes", "1_105");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GarrisonofGondorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Garrison of Gondor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Knight</b>. <b>Archer</b>.<br>To play, spot 3 [gondor] companions.<br>While you can spot a fortification, Garrison of Gondor is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("garrison");

		assertEquals("Garrison of Gondor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAThirdGondorCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetFreepsCard("garrison");
		var gknight = scn.GetFreepsCard("gknight");
		var kog = scn.GetFreepsCard("kog");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var rider = scn.GetFreepsCard("rider");
		// Two [gondor] companions, a Man and a Hobbit; beside them a [gondor] ally and a [rohan] companion (and
		// Frodo, a [shire] one), neither of whom counts.
		scn.MoveCompanionsToTable(gknight, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(garrison, kog);

		scn.StartGame();
		assertInZone(Zone.HAND, garrison, kog);
		assertFalse(scn.FreepsPlayAvailable(garrison));

		scn.FreepsPlayCard(kog);
		assertInZone(Zone.FREE_CHARACTERS, kog);
		assertTrue(scn.FreepsPlayAvailable(garrison));
		scn.FreepsPlayCard(garrison);
		assertInZone(Zone.FREE_CHARACTERS, garrison);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsDefenderPlusOneOnceAFortificationConditionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetFreepsCard("garrison");
		var gknight = scn.GetFreepsCard("gknight");
		var foes = scn.GetFreepsCard("foes");
		var firstlevel = scn.GetFreepsCard("firstlevel");
		scn.MoveCompanionsToTable(garrison, gknight);
		// A [gondor] condition that is no fortification.
		scn.MoveCardsToSupportArea(foes);
		scn.MoveCardsToHand(firstlevel);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, foes);
		assertFalse(scn.HasKeyword(garrison, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(garrison, Keyword.DEFENDER));

		scn.FreepsPlayCard(firstlevel);
		assertInZone(Zone.SUPPORT, firstlevel);
		assertTrue(scn.HasKeyword(garrison, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(garrison, Keyword.DEFENDER));
		// Another companion is not made a defender.
		assertFalse(scn.HasKeyword(gknight, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFortificationThatIsAPossessionOfAnotherCultureMakesHimDefenderPlusOneToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetFreepsCard("garrison");
		var stronghold = scn.GetFreepsCard("stronghold");
		scn.MoveCompanionsToTable(garrison);
		scn.MoveCardsToSupportArea(stronghold);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, stronghold);
		assertTrue(scn.HasKeyword(garrison, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(garrison, Keyword.DEFENDER));
	}
}
