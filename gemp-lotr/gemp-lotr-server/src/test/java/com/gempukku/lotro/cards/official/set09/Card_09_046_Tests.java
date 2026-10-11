package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arrow", "9_46");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, cost 2, vitality 3; Knight is his whole text
					put("knight2", "5_35");
					// Pippin, Trollslayer: a [gondor] companion who is a Hobbit, vitality 4.
					put("pippin", "103_31");
					// Gondorian Merchant: the only [gondor] ally, a Man of vitality 1, who can exert only with more.
					put("merchant", "7_97");
					// Narsil, Blade of the Faithful (a [gondor] artifact) and Ranger's Cloak (a [gondor] possession):
					// each vitality +1 and "Bearer must be a [gondor] Man"; their texts act on a wound, on Elendil or
					// Isildur, in a skirmish or on a ranger, none of which is here.
					put("narsil", "9_34");
					put("cloak", "15_68");
					put("guard", "5_83");       // Household Guard: [rohan] Man, vitality 3; Valiant is his whole text

					// Rider of Rohan: [rohan] Man, a companion, cost 2, vitality 3; his skirmish ability needs a mount.
					put("rider", "4_286");
					// Guma, Plains Farmer: [rohan] Man, an ally, cost 1, vitality 2; a fellowship ability only.
					put("guma", "4_277");
					// Theoden, Son of Thengel: [rohan] Man, a companion, cost 2, vitality 2; a regroup ability that
					// needs a [rohan] ally.
					put("theoden", "4_292");
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit (and a Man only beside 3 [rohan]
					// companions or Eowyn).  He stays in hand.
					put("merry", "103_85");

					put("spear", "4_288");      // Rider's Spear: [rohan] possession, cost 1; "Bearer must be a [rohan] Man."
					put("armor", "1_92");       // Armor: [gondor] possession, cost 1; "Bearer must be a Man."
					put("dwarvenarmor", "1_8"); // Dwarven Armor: a possession; "Bearer must be a Dwarf."
					// Brought Down from Inside: a [rohan] condition; "Bearer must be a [rohan] Man."
					put("brought", "19_24");
					// Herugrim (the Players' Council's): a [rohan] artifact, a hand weapon; "Bearer must be Theoden."
					put("herugrim", "54_280");

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is an assignment phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheRedArrowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: The Red Arrow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: <b>Assignment:</b> Exert a [gondor] Man to play a [rohan] Man. You may exert that [rohan] Man to play a possession on him or her. Discard this artifact.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arrow");

		assertEquals("The Red Arrow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheRedArrowExertsAGondorCompToPlayRohanCompDuringAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var knight2 = scn.GetFreepsCard("knight2");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var spear = scn.GetFreepsCard("spear");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(aragorn, knight, pippin, guard);
		// The Merchant, with his own 1 and two more from Narsil and the Cloak, is an ally with 3 vitality: he can exert.
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil, cloak);
		scn.MoveCardsToHand(arrow, rider, guma, merry, knight2, spear, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		// The Arrow costs 0, and nothing is needed to play it.
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(arrow);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, arrow);
		assertNull(arrow.getAttachedTo());

		// Not a maneuver ability.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arrow));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsActionAvailable(arrow));
		int pool = scn.GetTwilight();

		scn.FreepsUseCardAction(arrow);
		// The [gondor] Men, a companion or an ally; not the [gondor] Hobbit nor the [rohan] Man.
		assertEquals(3, scn.GetVitality(merchant));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merchant));

		// A [rohan] Man who is a companion and one who is an ally; not the [rohan] Hobbit, the [gondor] Man or
		// the [rohan] possession in the same hand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, knight2, spear));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(rider);
		assertInZone(Zone.FREE_CHARACTERS, rider);
		// The Rider costs 2.
		assertEquals(pool + 2, scn.GetTwilight());

		assertTrue(scn.FreepsDecisionAvailable("Would you like exert the played"));
		// The discard is the last thing the text does: the Arrow is still in play while the question is asked.
		assertInZone(Zone.SUPPORT, arrow);
		scn.FreepsChooseNo();

		// Declined: nobody else exerts, the possessions stay in hand, and the Arrow is discarded all the same.
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertInZone(Zone.HAND, spear, armor, guma);
		assertEquals(pool + 2, scn.GetTwilight());
		assertInDiscard(arrow);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void TheRedArrowCanOptionallyPlayPossessionOnPlayedCompByExerting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		var theoden = scn.GetFreepsCard("theoden");
		var spear = scn.GetFreepsCard("spear");
		var armor = scn.GetFreepsCard("armor");
		var dwarvenarmor = scn.GetFreepsCard("dwarvenarmor");
		var brought = scn.GetFreepsCard("brought");
		var herugrim = scn.GetFreepsCard("herugrim");
		var troop = scn.GetShadowCard("troop");
		// The Household Guard is another [rohan] Man, already in play, who could bear either possession.
		scn.MoveCompanionsToTable(aragorn, knight, guard);
		scn.MoveCardsToSupportArea(arrow);
		scn.MoveCardsToHand(theoden, spear, armor, dwarvenarmor, brought, herugrim);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		int pool = scn.GetTwilight();

		scn.FreepsUseCardAction(arrow);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));

		// Theoden is the only [rohan] Man in hand.
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsDecisionAvailable("Would you like exert the played"));
		scn.FreepsChooseYes();

		// The Man just played exerts, and no other [rohan] Man.
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(guard));

		// Possessions of two cultures that he may bear; not the possession he may not bear, nor the artifact
		// and the condition that he may.
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear, armor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwarvenarmor, herugrim, brought));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(spear);

		// On him, with no bearer asked for.
		assertAttachedTo(spear, theoden);
		assertNotAttachedTo(spear, guard);
		// 2 for Theoden and 1 for the Spear.
		assertEquals(pool + 3, scn.GetTwilight());
		assertInZone(Zone.HAND, armor, herugrim);
		assertInDiscard(arrow);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AnAllyIsARohanManToPlayAndAGondorPossessionMayBePlayedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(arrow);
		scn.MoveCardsToHand(guma, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		int pool = scn.GetTwilight();

		// Aragorn is the only [gondor] Man and Guma the only [rohan] Man in hand: neither is asked for.
		scn.FreepsUseCardAction(arrow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, guma);
		assertEquals(pool + 1, scn.GetTwilight());

		assertTrue(scn.FreepsDecisionAvailable("Would you like exert the played"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertAttachedTo(armor, guma);
		assertEquals(pool + 2, scn.GetTwilight());
		assertInDiscard(arrow);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileTheOnlyGondorManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var troop = scn.GetShadowCard("troop");
		// The [gondor] Hobbit and the [rohan] Man could both exert.
		scn.MoveCompanionsToTable(knight, pippin, guard);
		scn.MoveCardsToSupportArea(arrow);
		scn.MoveCardsToHand(rider);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// The Knight's vitality of 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(knight, 2);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arrow));

		scn.RemoveWoundsFromChar(knight, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(arrow));
	}

	@Test
	public void IsNotOfferedWithNoRohanManInHandToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var knight2 = scn.GetFreepsCard("knight2");
		var merry = scn.GetFreepsCard("merry");
		var spear = scn.GetFreepsCard("spear");
		var rider = scn.GetFreepsCard("rider");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(arrow);
		// A [gondor] Man, a [rohan] Hobbit and a [rohan] possession: each could be played, and none is a [rohan] Man.
		scn.MoveCardsToHand(knight2, merry, spear);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arrow));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.MoveCardsToHand(rider);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(arrow));
	}

	@Test
	public void WithNoPossessionInHandThatHeCanBearTheQuestionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var dwarvenarmor = scn.GetFreepsCard("dwarvenarmor");
		var brought = scn.GetFreepsCard("brought");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(arrow);
		// A possession the Rider may not bear, and a condition he may.
		scn.MoveCardsToHand(rider, dwarvenarmor, brought);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsActionAvailable(arrow));

		scn.FreepsUseCardAction(arrow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, rider);

		// Exerting him could play nothing: there is nothing to ask.
		assertFalse(scn.FreepsDecisionAvailable("Would you like exert the played"));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertInZone(Zone.HAND, dwarvenarmor, brought);
		assertInDiscard(arrow);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
