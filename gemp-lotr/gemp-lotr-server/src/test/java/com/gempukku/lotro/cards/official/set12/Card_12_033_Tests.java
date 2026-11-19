package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_033_Tests
{

	// A Shadows-block path, begun at Fangorn Glade 11_239 (a forest, no battleground or dwelling).  The Shadow player
	// lays site 2 at the first move: Streets of Bree 18_140 (a dwelling; its only text is about followers), Fortress
	// of Orthanc 11_241 (a battleground; it adds (2) for each companion over 5, and there are never more than 4) or
	// Sirannon Ruins 18_138 (a river and nothing else).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("terror", "12_33");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: a [gandalf] Wizard companion, strength 8, vitality 4, no text
					// Ent Avenger: a [gandalf] Ent companion who is no Gandalf, strength 6; +2 only beside 3 Ents
					put("avenger", "6_27");
					// Barliman Butterbur: a [gandalf] ally, which is no companion; a fellowship ability only
					put("barliman", "1_70");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, strength 8; a maneuver ability only

					// Gandalf's Wisdom and Unknown Perils: [gandalf] conditions (a discount, and a response used only on purpose)
					put("wisdom", "2_23");
					put("perils", "3_36");
					put("glamdring", "1_75");   // Glamdring: a [gandalf] possession, which is no condition
					put("lord", "1_21");        // Lord of Moria: a [dwarven] condition
					// The Nine Walkers: a [gandalf] condition, laid in the Shadow player's own discard pile
					put("walkers", "1_79");

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site6", "18_140");     // Streets of Bree, in place of Buckland Homestead
					put("site9", "11_241");     // Fortress of Orthanc
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void TheTerrorofHisComingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: The Terror of His Coming
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [gandalf] companion strength +2. Then, if that companion is at a battleground or dwelling site, you may take a [gandalf] condition from your discard pile into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("terror");

		assertEquals("The Terror of His Coming", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtADwellingTheChosenCompanionIsStrengthPlus2AndAGandalfConditionMayBeTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetFreepsCard("terror");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wisdom = scn.GetFreepsCard("wisdom");
		var perils = scn.GetFreepsCard("perils");
		var glamdring = scn.GetFreepsCard("glamdring");
		var lord = scn.GetFreepsCard("lord");
		var walkers = scn.GetShadowCard("walkers");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(terror);
		scn.MoveCardsToDiscard(wisdom, perils, glamdring, lord, walkers);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertInDiscard(wisdom, perils, glamdring, lord, walkers);

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site6"));
		assertEquals("Streets of Bree", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		scn.FreepsPlayCard(terror);

		// The [gandalf] companions, in the skirmish or not; not an ally, nor a companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(avenger);

		assertEquals(8, scn.GetStrength(avenger));
		assertEquals(8, scn.GetStrength(gandalf));

		// At a dwelling: the [gandalf] conditions of the player's own discard pile; not a [gandalf] possession, a
		// condition of another culture or the Shadow player's [gandalf] condition in his own pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wisdom, perils));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(glamdring, lord, walkers));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(wisdom);

		assertInHand(wisdom);
		assertInDiscard(perils, glamdring, lord, walkers, terror);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtABattlegroundAConditionMayBeTakenAndTheBonusLastsTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetFreepsCard("terror");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wisdom = scn.GetFreepsCard("wisdom");
		var perils = scn.GetFreepsCard("perils");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(terror);
		scn.MoveCardsToDiscard(wisdom, perils);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertInDiscard(wisdom, perils);

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site9"));
		assertEquals("Fortress of Orthanc", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(8, scn.GetStrength(gandalf));
		scn.FreepsPlayCard(terror);

		// Gandalf, the one [gandalf] companion, is chosen unasked.
		assertEquals(10, scn.GetStrength(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(wisdom, perils));
		scn.FreepsChooseCard(perils);

		assertInHand(perils);
		assertInDiscard(wisdom);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 10 against 13: Gandalf loses and takes a wound, and the bonus is gone with his skirmish phase.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(8, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtASiteThatIsNeitherNothingIsAskedAfterTheBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetFreepsCard("terror");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wisdom = scn.GetFreepsCard("wisdom");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(terror);
		scn.MoveCardsToDiscard(wisdom);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertInDiscard(wisdom);

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));
		assertEquals("Sirannon Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(8, scn.GetStrength(gandalf));
		scn.FreepsPlayCard(terror);

		assertEquals(10, scn.GetStrength(gandalf));
		assertInDiscard(wisdom, terror);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGandalfConditionInTheDiscardPileNothingIsAskedAtADwelling() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetFreepsCard("terror");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var lord = scn.GetFreepsCard("lord");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(terror);
		scn.MoveCardsToDiscard(glamdring, lord);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertInDiscard(glamdring, lord);

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site6"));
		assertEquals("Streets of Bree", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(8, scn.GetStrength(gandalf));
		scn.FreepsPlayCard(terror);

		assertEquals(10, scn.GetStrength(gandalf));
		assertInDiscard(glamdring, lord, terror);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DecliningTheTakeLeavesTheConditionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetFreepsCard("terror");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wisdom = scn.GetFreepsCard("wisdom");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(terror);
		scn.MoveCardsToDiscard(wisdom);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertInDiscard(wisdom);

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site6"));
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPlayCard(terror);

		assertEquals(10, scn.GetStrength(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(wisdom));
		scn.FreepsDeclineChoosing();

		assertInDiscard(wisdom, terror);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetFreepsCard("terror");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(terror);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertInHand(terror);

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site6"));
		scn.MoveMinionsToTable(troop);

		// The phase before the skirmishes: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(terror));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.FreepsPlayAvailable(terror));
		assertInHand(terror);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoGandalfCompanionItIsPlayedForNothingAndTakesNothingAtADwelling() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetFreepsCard("terror");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var wisdom = scn.GetFreepsCard("wisdom");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(terror);
		scn.MoveCardsToDiscard(wisdom);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertInDiscard(wisdom);

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site6"));
		assertEquals("Streets of Bree", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		// Its target is no requirement of its play (CR 4.2 "effect").
		assertTrue(scn.FreepsPlayAvailable(terror));
		scn.FreepsPlayCard(terror);

		// No [gandalf] companion was made stronger, so there is no "that companion" to be at the dwelling.
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.FreepsHasCardChoiceAvailable(wisdom));
		assertInDiscard(wisdom, terror);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
