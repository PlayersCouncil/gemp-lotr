package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_056_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trap", "103_56");
					put("band", "1_270");         // Orc Scouting Band: [sauron] Orc, tracker, strength 8; a skirmish ability never used here
					put("scout", "4_252");        // Southron Scout: Southron, ambush (2), strength 3, no other text
					put("dunlending", "11_79");   // Fearsome Dunlending: [men] Man, ambush (1), strength 9; its text is only for when it is played
					put("explorer", "4_250");     // Southron Explorer: Southron with neither tracker nor ambush, strength 5, vitality 3
					put("explorer2", "4_250");
					put("explorer3", "4_250");
					put("infantry", "4_227");     // Easterling Infantry: [raider] Man and no Southron, strength 8; no text below 3 burdens
					put("stalker", "103_49");     // Desert Wind Stalker: Southron, tracker, strength 12; "Southrons gain ambush (1)"
					put("galley", "8_59");        // Corsair War Galley: a [raider] possession; its text is not used here
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, no text at all

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4
					put("guard", "1_7");          // Dwarf Guard: strength 4, vitality 2, no text in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SandcraftTrapStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sandcraft Trap
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Restore all trackers and minions with ambush.  If you restored any, make each Southron
		 * 	<b>ambush (1)</b> and strength +1 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trap");

		assertEquals("Sandcraft Trap", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RestoresEveryTrackerAndEveryMinionWithAmbushAndMakesEachSouthronAmbush1AndStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var band = scn.GetShadowCard("band");
		var scout = scn.GetShadowCard("scout");
		var dunlending = scn.GetShadowCard("dunlending");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var infantry = scn.GetShadowCard("infantry");
		var troop = scn.GetShadowCard("troop");
		var galley = scn.GetShadowCard("galley");
		scn.MoveCardsToHand(trap);
		scn.MoveCardsToSupportArea(galley);

		scn.StartGame();
		scn.MoveMinionsToTable(band, scout, dunlending, explorer, explorer2, infantry, troop);
		// Every Southron there is is a [raider] Man: an Orc is made one, to show that the text goes by the keyword.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.SOUTHRON));
		// Hindered: a tracker with no ambush, a Southron with ambush who is no tracker, a minion with ambush
		// who is no Southron, and two cards that are neither: a Southron minion and a [raider] possession.
		scn.HinderCard(band, scout, dunlending, explorer2, galley);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.IsHindered(band));
		assertTrue(scn.IsHindered(scout));
		assertTrue(scn.IsHindered(dunlending));
		assertTrue(scn.IsHindered(explorer2));
		assertTrue(scn.IsHindered(galley));
		assertEquals(5, scn.GetStrength(explorer));
		assertFalse(scn.HasKeyword(explorer, Keyword.AMBUSH));
		assertEquals(8, scn.GetStrength(infantry));
		assertEquals(13, scn.GetStrength(troop));
		assertFalse(scn.HasKeyword(troop, Keyword.AMBUSH));
		assertInZone(Zone.HAND, trap);
		assertTrue(scn.ShadowPlayAvailable(trap));

		scn.ShadowPlayCard(trap);

		// 5 - 3 for the event
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, trap);

		assertFalse(scn.IsHindered(band));
		assertFalse(scn.IsHindered(scout));
		assertFalse(scn.IsHindered(dunlending));
		assertTrue(scn.IsHindered(explorer2));
		assertTrue(scn.IsHindered(galley));

		// A Southron who was never hindered: strength 5 + 1, and ambush (1)
		assertEquals(6, scn.GetStrength(explorer));
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		// A Southron this event restored: strength 3 + 1, and ambush (1) beside the ambush (2) printed on him
		assertEquals(4, scn.GetStrength(scout));
		assertEquals(3, scn.GetKeywordCount(scout, Keyword.AMBUSH));
		// A Southron who is neither [raider] nor a Man: strength 13 + 1
		assertEquals(14, scn.GetStrength(troop));
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.AMBUSH));
		// A [raider] Man who is no Southron
		assertEquals(8, scn.GetStrength(infantry));
		assertFalse(scn.HasKeyword(infantry, Keyword.AMBUSH));
		// Restored, and no Southron
		assertEquals(8, scn.GetStrength(band));
		assertFalse(scn.HasKeyword(band, Keyword.AMBUSH));
		assertEquals(9, scn.GetStrength(dunlending));
		assertEquals(1, scn.GetKeywordCount(dunlending, Keyword.AMBUSH));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AHinderedSouthronWhoWouldOnlyGetAmbushFromAnotherCardIsNotRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var stalker = scn.GetShadowCard("stalker");
		var scout = scn.GetShadowCard("scout");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		scn.MoveCardsToHand(trap);

		scn.StartGame();
		// Stalker: "Southrons gain ambush (1)". The Explorer would have it if he were not hindered.
		scn.MoveMinionsToTable(stalker, scout, explorer, explorer2);
		// The second Explorer was given ambush by an effect aimed at him alone before he was hindered, as
		// Honor of the Desert Warrior gives its Southron ambush (4) until the regroup phase.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, explorer2, null, Keyword.AMBUSH, 4));
		// 4 given to him + 1 from the Stalker
		assertEquals(5, scn.GetKeywordCount(explorer2, Keyword.AMBUSH));
		scn.HinderCard(scout, explorer, explorer2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(scout));
		assertTrue(scn.IsHindered(explorer));
		assertTrue(scn.IsHindered(explorer2));
		assertTrue(scn.ShadowPlayAvailable(trap));

		scn.ShadowPlayCard(trap);

		// Ambush is printed on the Scout and on neither Explorer.
		assertFalse(scn.IsHindered(scout));
		assertTrue(scn.IsHindered(explorer));
		assertTrue(scn.IsHindered(explorer2));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNothingHinderedToRestoreTheEventIsStillPlayedAndNoSouthronChanges() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var band = scn.GetShadowCard("band");
		var scout = scn.GetShadowCard("scout");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		scn.MoveCardsToHand(trap);

		scn.StartGame();
		// A tracker and a minion with ambush, neither of them hindered, and a hindered Southron who is neither.
		scn.MoveMinionsToTable(band, scout, explorer, explorer2);
		scn.HinderCard(explorer2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertFalse(scn.IsHindered(band));
		assertFalse(scn.IsHindered(scout));
		assertTrue(scn.IsHindered(explorer2));
		assertEquals(5, scn.GetStrength(explorer));
		assertFalse(scn.HasKeyword(explorer, Keyword.AMBUSH));
		assertEquals(3, scn.GetStrength(scout));
		assertEquals(2, scn.GetKeywordCount(scout, Keyword.AMBUSH));
		assertTrue(scn.ShadowPlayAvailable(trap));

		scn.ShadowPlayCard(trap);

		// 5 - 3: the event is paid for and discarded all the same
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, trap);
		assertTrue(scn.IsHindered(explorer2));
		assertEquals(5, scn.GetStrength(explorer));
		assertFalse(scn.HasKeyword(explorer, Keyword.AMBUSH));
		assertEquals(3, scn.GetStrength(scout));
		assertEquals(2, scn.GetKeywordCount(scout, Keyword.AMBUSH));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OneRestoredTrackerIsEnoughAndTheBonusLastsThroughTheSkirmishesUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var band = scn.GetShadowCard("band");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToHand(trap);

		scn.StartGame();
		scn.MoveMinionsToTable(band, explorer, explorer2);
		scn.HinderCard(band);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(band));
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(5, scn.GetStrength(explorer2));

		scn.ShadowPlayCard(trap);

		// The one card restored is a [sauron] Orc, and both Southrons have the bonus.
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.IsHindered(band));
		assertEquals(6, scn.GetStrength(explorer));
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertEquals(6, scn.GetStrength(explorer2));
		assertEquals(1, scn.GetKeywordCount(explorer2, Keyword.AMBUSH));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ aragorn, explorer }, new PhysicalCardImpl[]{ guard, explorer2 });

		// The ambush is real: each Southron the Free Peoples player assigned offers its (1).
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(explorer));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(explorer2));
		scn.ShadowChooseOptionalTrigger(explorer);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(explorer2));
		scn.ShadowChooseOptionalTrigger(explorer2);
		assertEquals(2, scn.GetTwilight());

		// The restored Orc is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Aragorn's 8 beat the Explorer's 6.
		assertEquals(1, scn.GetWoundsOn(explorer));

		scn.FreepsResolveSkirmish(guard);

		// In the second skirmish, after the first has been resolved
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(explorer));
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertEquals(6, scn.GetStrength(explorer2));
		assertEquals(1, scn.GetKeywordCount(explorer2, Keyword.AMBUSH));

		scn.PassCurrentPhaseActions();

		// The second Explorer's 6 beat the Dwarf Guard's 4.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetStrength(explorer));
		assertFalse(scn.HasKeyword(explorer, Keyword.AMBUSH));
		assertEquals(5, scn.GetStrength(explorer2));
		assertFalse(scn.HasKeyword(explorer2, Keyword.AMBUSH));
	}

	@Test
	public void ASouthronWhoIsRestoredOrArrivesAfterTheEventWasPlayedGetsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var scout = scn.GetShadowCard("scout");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var explorer3 = scn.GetShadowCard("explorer3");
		scn.MoveCardsToHand(trap);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, explorer, explorer2);
		scn.HinderCard(scout, explorer2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.ShadowPlayCard(trap);

		assertFalse(scn.IsHindered(scout));
		assertEquals(6, scn.GetStrength(explorer));
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		// Neither a tracker nor a minion with ambush, so still hindered
		assertTrue(scn.IsHindered(explorer2));

		// One Southron comes back from being hindered and another comes into play, both after the event.
		scn.RestoreCard(explorer2);
		scn.MoveMinionsToTable(explorer3);

		assertFalse(scn.IsHindered(explorer2));
		assertEquals(5, scn.GetStrength(explorer2));
		assertFalse(scn.HasKeyword(explorer2, Keyword.AMBUSH));
		assertEquals(5, scn.GetStrength(explorer3));
		assertFalse(scn.HasKeyword(explorer3, Keyword.AMBUSH));
		// The Southron who was there keeps what he was given.
		assertEquals(6, scn.GetStrength(explorer));
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void CanBePlayedInTheAssignmentPhaseAndNotInTheArcheryOrSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var scout = scn.GetShadowCard("scout");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(trap);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, explorer);
		scn.HinderCard(scout);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(trap));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(trap));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsHindered(scout));
		assertFalse(scn.ShadowPlayAvailable(trap));
	}
}
