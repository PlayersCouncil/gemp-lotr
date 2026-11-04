package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crown", "18_41");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("rohanaragorn", "17_93"); // Aragorn, Defender of Rohan: the [rohan] Aragorn; hunter for [rohan] companions only while the Ring-bearer is assigned
					put("elendil", "9_32");     // Elendil, The Tall: [gondor] Man; his abilities are used only on purpose
					put("isildur", "9_33");     // Isildur, Bearer of Heirlooms: [gondor] Man; his texts need him to be the Ring-bearer or to bear an artifact
					put("knight1", "5_35");     // Gondorian Knight: [gondor] Man companion of another title, vitality 3; Knight is his whole text
					put("knight2", "5_35");
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: a [rohan] Man, vitality 3; his strength text reads a skirmish
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit), vitality 4; enduring, and unwounded here

					put("path1", "8_42");       // A Path Appointed: a [gondor] card to hold in hand
					put("path2", "8_42");
					put("guard", "1_7");        // Dwarf Guard: a card of another culture to hold in hand

					put("runner1", "1_178");    // Goblin Runner: a minion with no text once in play, strength 5
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CrownofGondorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Crown of Gondor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: To play, spot Aragorn, Elendil, or Isildur.<br><b>Skirmish:</b> Spot a [gondor] Man with 3 or more vitality and discard a [gondor] card from hand to make that Man strength +2.
		*/

		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");

		assertEquals("Crown of Gondor", crown.getBlueprint().getTitle());
		assertNull(crown.getBlueprint().getSubtitle());
		assertTrue(crown.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, crown.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, crown.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, crown.getBlueprint().getCardType());
		assertNull(crown.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(crown, Keyword.SUPPORT_AREA));
		assertEquals(4, crown.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAragornIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToHand(crown, aragorn);
		assertInHand(crown, aragorn);

		scn.StartGame();

		// Frodo and a [gondor] companion of another title: nobody to spot.
		assertFalse(scn.FreepsPlayAvailable(crown));

		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.FreepsPlayAvailable(crown));

		scn.FreepsPlayCard(crown);
		assertInZone(Zone.SUPPORT, crown);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedBySpottingTheRohanAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		scn.MoveCompanionsToTable(rohanaragorn);
		scn.MoveCardsToHand(crown);
		assertInHand(crown);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(crown));

		scn.FreepsPlayCard(crown);
		assertInZone(Zone.SUPPORT, crown);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedBySpottingElendil() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var elendil = scn.GetFreepsCard("elendil");
		scn.MoveCompanionsToTable(elendil);
		scn.MoveCardsToHand(crown);
		assertInHand(crown);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(crown));

		scn.FreepsPlayCard(crown);
		assertInZone(Zone.SUPPORT, crown);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedBySpottingIsildur() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var isildur = scn.GetFreepsCard("isildur");
		scn.MoveCompanionsToTable(isildur);
		scn.MoveCardsToHand(crown);
		assertInHand(crown);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(crown));

		scn.FreepsPlayCard(crown);
		assertInZone(Zone.SUPPORT, crown);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilitySpotsAGondorManWith3VitalityAndDiscardsAGondorCardToMakeHimStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var eomer = scn.GetFreepsCard("eomer");
		var pippin = scn.GetFreepsCard("pippin");
		var path1 = scn.GetFreepsCard("path1");
		var path2 = scn.GetFreepsCard("path2");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, knight1, knight2, eomer, pippin);
		scn.MoveCardsToSupportArea(crown);
		scn.MoveCardsToHand(path1, path2, guard);
		assertInHand(path1, path2, guard);
		// Aragorn, vitality 4, with 3 left; the second Knight, vitality 3, with 2 left.
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(knight2, 1);
		assertEquals(3, scn.GetVitality(aragorn));
		assertEquals(3, scn.GetVitality(knight1));
		assertEquals(2, scn.GetVitality(knight2));
		assertEquals(3, scn.GetVitality(eomer));
		assertEquals(4, scn.GetVitality(pippin));

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{eomer, runner2});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(knight1));
		assertTrue(scn.FreepsActionAvailable(crown));

		scn.FreepsUseCardAction(crown);
		// [gondor] Men with 3 or more vitality, in the skirmish and in none; not the Knight with 2 left, the [rohan]
		// Man, the [gondor] Hobbit or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight2, eomer, pippin, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		// The [gondor] cards in hand; not the [dwarven] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(path1, path2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard));
		scn.FreepsChooseCard(path1);

		assertInDiscard(path1);
		assertInHand(path2, guard);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(knight1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(eomer);
		// In the next skirmish, a phase of its own, the bonus is gone.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithEveryGondorManBelow3Vitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var aragorn = scn.GetFreepsCard("aragorn");
		var eomer = scn.GetFreepsCard("eomer");
		var pippin = scn.GetFreepsCard("pippin");
		var path1 = scn.GetFreepsCard("path1");
		scn.MoveCompanionsToTable(aragorn, eomer, pippin);
		scn.MoveCardsToSupportArea(crown);
		scn.MoveCardsToHand(path1);
		assertInHand(path1);
		// Aragorn, vitality 4, with 2 left; the [rohan] Man and the [gondor] Hobbit have 3 and 4.
		scn.AddWoundsToChar(aragorn, 2);
		assertEquals(2, scn.GetVitality(aragorn));
		assertEquals(3, scn.GetVitality(eomer));
		assertEquals(4, scn.GetVitality(pippin));

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(crown));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoGondorCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetFreepsCard("crown");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(crown);
		scn.MoveCardsToHand(guard);
		assertInHand(guard);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn has all 4 vitality; the only card in hand is [dwarven].
		assertEquals(4, scn.GetVitality(aragorn));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(crown));
	}
}
