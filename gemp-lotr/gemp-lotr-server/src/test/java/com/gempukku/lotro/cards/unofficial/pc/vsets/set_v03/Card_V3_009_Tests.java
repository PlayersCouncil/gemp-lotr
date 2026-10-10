package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_009_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enter", "103_9");
					put("enter2", "103_9");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: vitality 4; a fellowship ability never used here
					put("radagast", "9_26");    // Radagast, The Brown: the other Free Peoples Wizard, vitality 4
					put("quickbeam", "6_33");   // Quickbeam, Bregalad: a [gandalf] companion who is an Ent and no Wizard
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; Maneuver: exert him to make him defender +1
					put("whitesaruman", "32_24");   // Saruman, The White (a Hobbit-set card): a [gandalf] ally who is a Wizard, vitality 4; makes Gandalf defender +1, fellowship and skirmish abilities only
					put("toby", "1_305");       // Old Toby: a Free Peoples possession in the support area
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc with no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 and nothing else
					put("saruman", "3_69");     // Saruman, Servant of the Eye: the opponent's Wizard, an [isengard] minion, vitality 4; an assignment ability only
					put("armory", "1_173");     // Goblin Armory: Shadow condition in the support area
					put("stone", "9_47");       // Ithil Stone: Shadow artifact in the support area; may draw at the start of the Shadow phase
					put("ships", "8_65");       // Ships of Great Draught: Shadow possession in the support area; Shadow and regroup abilities
					put("scimitar", "1_180");   // Goblin Scimitar: Shadow possession borne by a [moria] Orc
					put("poison", "8_28");      // Spider Poison: Shadow possession a companion bears; does nothing until its bearer is assigned
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YouCannotEnterHereStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: You Cannot Enter Here
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert your Wizard to hinder a Shadow card. Exhaust that Wizard if you chose a minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enter");

		assertEquals("You Cannot Enter Here", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertFalse(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheWizardToHinderAnyShadowCardAndOneThatIsNoMinionCostsHimNothingMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enter = scn.GetFreepsCard("enter");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var toby = scn.GetFreepsCard("toby");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var armory = scn.GetShadowCard("armory");
		var stone = scn.GetShadowCard("stone");
		var ships = scn.GetShadowCard("ships");
		var scimitar = scn.GetShadowCard("scimitar");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToHand(enter);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.AttachCardsTo(troop, scimitar);
		scn.AttachCardsTo(aragorn, poison);
		scn.MoveCardsToSupportArea(armory, stone, ships);
		scn.SkipToPhase(Phase.MANEUVER);

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.HAND, enter);
		assertTrue(scn.FreepsPlayAvailable(enter));

		scn.FreepsPlayCard(enter);

		assertEquals(twilight + 2, scn.GetTwilight());
		// The only Wizard exerts.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// Minions of two cultures; a condition, an artifact and a possession in the support area; a possession
		// on a minion and one on a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage, armory, stone, ships, scimitar, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, aragorn, frodo, toby));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		// A possession that a minion bears is not a minion.
		scn.FreepsChooseCard(scimitar);

		assertTrue(scn.IsHindered(scimitar));
		assertFalse(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(savage));
		assertFalse(scn.IsHindered(armory));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.DISCARD, enter);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ChoosingAMinionHindersItAndExhaustsTheWizard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enter = scn.GetFreepsCard("enter");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enter);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.MoveCardsToSupportArea(armory);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertFalse(scn.IsHindered(troop));

		scn.FreepsPlayCard(enter);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage, armory));

		scn.FreepsChooseCard(troop);

		assertTrue(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(savage));
		assertFalse(scn.IsHindered(armory));
		// Vitality 4: the exertion that paid, then two more.
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsExhausted(gandalf));
		assertInZone(Zone.DISCARD, enter);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OnlyAWizardOfYourOwnCanExertAndTheOneWhoDidIsTheOneExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enter = scn.GetFreepsCard("enter");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var whitesaruman = scn.GetFreepsCard("whitesaruman");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, radagast, quickbeam, aragorn);
		// A Wizard of yours who is an ally: the text does not say "companion".
		scn.MoveAlliesToTable(whitesaruman);
		scn.MoveCardsToHand(enter);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(enter);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast, whitesaruman));
		// Quickbeam is [gandalf] and no Wizard; Aragorn is a companion who is neither; the minion Saruman is a
		// Wizard and not yours.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(quickbeam, aragorn, saruman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(radagast);

		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, troop));

		// The minion chosen is itself a Wizard: it is hindered, and the Wizard exhausted is still Radagast.
		scn.FreepsChooseCard(saruman);

		assertTrue(scn.IsHindered(saruman));
		assertFalse(scn.IsHindered(troop));
		assertEquals(3, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(whitesaruman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertInZone(Zone.DISCARD, enter);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheOnlyWizardOfYoursIsExhaustedAndWithOneExertionLeftAMinionIsStillHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enter = scn.GetFreepsCard("enter");
		var gandalf = scn.GetFreepsCard("gandalf");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, quickbeam, aragorn);
		scn.MoveCardsToHand(enter);

		scn.StartGame();
		// A Wizard who could exert, but the opponent's.
		scn.MoveMinionsToTable(saruman, troop);
		// Vitality 4 with 3 wounds
		scn.AddWoundsToChar(gandalf, 3);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsExhausted(gandalf));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(enter));

		// One wound fewer, and another action taken so that the game asks afresh.
		scn.RemoveWoundsFromChar(gandalf, 1);
		scn.FreepsUseCardAction(aragorn);
		scn.ShadowPass();

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(enter));

		scn.FreepsPlayCard(enter);

		// The exertion that pays leaves him exhausted, and a minion may be chosen all the same: exhausting him
		// is something the event does afterwards, not something it needs.
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, troop));

		scn.FreepsChooseCard(troop);

		assertTrue(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(saruman));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertInZone(Zone.DISCARD, enter);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AShadowCardAlreadyHinderedIsNotOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enter = scn.GetFreepsCard("enter");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var armory = scn.GetShadowCard("armory");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enter);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.MoveCardsToSupportArea(armory, ships);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.HinderCard(savage, armory);

		assertTrue(scn.IsHindered(savage));
		assertTrue(scn.IsHindered(armory));

		scn.FreepsPlayCard(enter);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, ships));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, armory));

		scn.FreepsChooseCard(ships);

		assertTrue(scn.IsHindered(ships));
		assertFalse(scn.IsHindered(troop));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void StillPlayableForItsCostWhenNoShadowCardIsLeftToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enter = scn.GetFreepsCard("enter");
		var enter2 = scn.GetFreepsCard("enter2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, radagast);
		scn.MoveCardsToHand(enter, enter2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// The first copy hinders the only Shadow card in play and exhausts Gandalf.
		scn.FreepsPlayCard(enter);
		scn.FreepsChooseCard(gandalf);

		assertTrue(scn.IsHindered(troop));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();

		// An event is offered whenever its cost can be paid, whatever it will then do.
		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsPlayAvailable(enter2));

		scn.FreepsPlayCard(enter2);

		// Radagast is the only Wizard who can still exert. Nothing is hindered and nobody is exhausted.
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsHindered(troop));
		assertInZone(Zone.DISCARD, enter, enter2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
