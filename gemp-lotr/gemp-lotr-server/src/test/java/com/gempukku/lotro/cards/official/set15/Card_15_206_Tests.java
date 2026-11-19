package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_206_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hunters", "15_206");

					put("aragorn", "4_109");        // Aragorn, Heir of Elendil: [gondor] companion, no hunter; Defender +1 only
					put("hunteraragorn", "15_54");  // Aragorn, Swift Hunter: [gondor] companion, Ranger, Hunter 1; a Skirmish ability never used here
					put("rohanaragorn", "17_93");   // Aragorn, Defender of Rohan: [rohan] companion, no hunter while the Ring-bearer is unassigned
					put("legolas", "11_21");        // Legolas, Companion of the Ring: no hunter, no archer; +2 only at a battleground or forest
					put("hunterlegolas", "15_18");  // Legolas, Fleet-footed Hunter: Hunter 1; a Regroup ability never used here
					put("gimli", "0_62");           // Gimli, Dwarven Delegate: no hunter; Damage +1 only
					put("huntergimli", "15_5");     // Gimli, Eager Hunter: Damage +1, Hunter 3, no other text
					put("elf", "1_53");             // Lórien Elf: [elven] Elf companion who is no Legolas
					put("knight", "5_35");          // Gondorian Knight: [gondor] Man companion who is no Aragorn
					put("dwarf", "1_7");            // Dwarf Guard: [dwarven] Dwarf companion who is no Gimli

					put("troop", "1_177");          // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "4_17");          // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					put("brigand", "4_10");         // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForththeThreeHuntersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Forth the Three Hunters!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Archery:</b> Exert Legolas and exert Aragorn to wound a minion.<br><b>Skirmish:</b> Exert Aragorn and spot Gimli to make a minion strength -1 (or -3 if Aragorn and Gimli are hunters).<br><b>Regroup:</b> Exert Gimli and spot Legolas to draw a card (or 2 cards if Gimli and Legolas are hunters).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hunters");

		assertEquals("Forth the Three Hunters!", card.getBlueprint().getTitle());
		// Not printed on the card: the one printed card (15C60) of three cultures stands in Gemp as three versions.
		assertEquals("Gondor Version", card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ArcheryAbilityExertsLegolasAndAragornToWoundAMinionOfEitherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(hunters);
		scn.MoveCompanionsToTable(legolas, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, brigand);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInPlay(brigand);
		assertTrue(scn.FreepsActionAvailable(hunters));

		scn.FreepsUseCardAction(hunters);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Minions of two cultures and races are offered, and none of the companions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		// The Brigand has 1 vitality: a wound kills him, where an exertion could not be placed at all.
		scn.FreepsChooseCard(brigand);
		assertInZone(Zone.DISCARD, brigand);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void TheRohanAragornPaysTheArcheryExertionToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var legolas = scn.GetFreepsCard("hunterlegolas");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(hunters);
		scn.MoveCompanionsToTable(legolas, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(hunters));

		scn.FreepsUseCardAction(hunters);
		// The Troop, the only minion, is wounded unasked.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileLegolasOrAragornIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elf = scn.GetFreepsCard("elf");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(hunters);
		scn.MoveCompanionsToTable(legolas, aragorn, elf, knight);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Legolas exhausted (vitality 3, 2 wounds); another [elven] Elf companion beside him could exert.
		scn.AddWoundsToChar(legolas, 2);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(elf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsActionAvailable(hunters));

		// Aragorn exhausted (vitality 4, 3 wounds) and Legolas able again; another [gondor] Man companion could exert.
		scn.RemoveWoundsFromChar(legolas, 2);
		scn.AddWoundsToChar(aragorn, 3);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.FreepsActionAvailable(hunters));

		// Both able: offered.
		scn.RemoveWoundsFromChar(aragorn, 3);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(hunters));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsAragornToMakeAnyMinionStrengthMinus1UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(hunters);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gimli, savage});
		// The Brigand is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(11, scn.GetStrength(savage));
		assertTrue(scn.FreepsActionAvailable(hunters));

		scn.FreepsUseCardAction(hunters);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Gimli is only spotted, not exerted.
		assertEquals(0, scn.GetWoundsOn(gimli));
		// Any minion: the one in this skirmish, one assigned to a skirmish still to come and one unassigned, of two
		// cultures and races; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(savage);
		// Neither Aragorn nor Gimli is a hunter: -1.
		assertEquals(10, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The penalty lasts the skirmish it was made in: in the next, the Savage is at his printed strength.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(11, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesAMinionStrengthMinus3WhileAragornAndGimliAreHunters() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var aragorn = scn.GetFreepsCard("hunteraragorn");
		var gimli = scn.GetFreepsCard("huntergimli");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(hunters);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsUseCardAction(hunters);
		// The Troop, the only minion, is chosen unasked.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Gimli is only spotted, not exerted.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(10, scn.GetStrength(troop));   // 13 - 3
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesAMinionStrengthMinus1WhileGimliIsNoHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var aragorn = scn.GetFreepsCard("hunteraragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("hunterlegolas");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(hunters);
		// A second hunter companion beside the hunter Aragorn, who is no Gimli.
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsUseCardAction(hunters);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Gimli is only spotted, not exerted.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(12, scn.GetStrength(troop));   // 13 - 1
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheRohanAragornPaysTheSkirmishExertionAndBeingNoHunterGivesMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var gimli = scn.GetFreepsCard("huntergimli");
		var legolas = scn.GetFreepsCard("hunterlegolas");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(hunters);
		// A second hunter companion beside the hunter Gimli, who is no Aragorn.
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		// The Ring-bearer is not assigned, so this Aragorn is no hunter.
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsUseCardAction(hunters);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Gimli is only spotted, not exerted.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(12, scn.GetStrength(troop));   // 13 - 1
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithoutGimli() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarf = scn.GetFreepsCard("dwarf");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(hunters);
		// A Dwarf companion who is no Gimli.
		scn.MoveCompanionsToTable(aragorn, dwarf);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsActionAvailable(hunters));

		scn.MoveCompanionsToTable(gimli);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(hunters));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileAragornIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(hunters);
		scn.MoveCompanionsToTable(aragorn, gimli, knight);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop);
		scn.FreepsResolveSkirmish(knight);

		// Aragorn exhausted (vitality 4, 3 wounds); another [gondor] Man companion beside him could exert.
		scn.AddWoundsToChar(aragorn, 3);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.FreepsActionAvailable(hunters));

		scn.RemoveWoundsFromChar(aragorn, 3);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(hunters));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsGimliToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(hunters);
		scn.MoveCompanionsToTable(gimli, legolas);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		int hand = scn.GetFreepsHandCount();
		assertTrue(scn.FreepsActionAvailable(hunters));

		scn.FreepsUseCardAction(hunters);
		// Neither is a hunter: one card.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityDrawsTwoCardsWhileGimliAndLegolasAreHunters() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var gimli = scn.GetFreepsCard("huntergimli");
		var legolas = scn.GetFreepsCard("hunterlegolas");
		scn.MoveCardsToSupportArea(hunters);
		scn.MoveCompanionsToTable(gimli, legolas);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(gimli));
		int hand = scn.GetFreepsHandCount();

		scn.FreepsUseCardAction(hunters);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(hand + 2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityDrawsOneCardWhileLegolasIsNoHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var gimli = scn.GetFreepsCard("huntergimli");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("hunteraragorn");
		scn.MoveCardsToSupportArea(hunters);
		// A second hunter companion beside the hunter Gimli, who is no Legolas.
		scn.MoveCompanionsToTable(gimli, legolas, aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(gimli));
		int hand = scn.GetFreepsHandCount();

		scn.FreepsUseCardAction(hunters);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityDrawsOneCardWhileGimliIsNoHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("hunterlegolas");
		var aragorn = scn.GetFreepsCard("hunteraragorn");
		scn.MoveCardsToSupportArea(hunters);
		// A second hunter companion beside the hunter Legolas, who is no Gimli.
		scn.MoveCompanionsToTable(gimli, legolas, aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(gimli));
		int hand = scn.GetFreepsHandCount();

		scn.FreepsUseCardAction(hunters);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithoutLegolas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(hunters);
		// An Elf companion who is no Legolas.
		scn.MoveCompanionsToTable(gimli, elf);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsActionAvailable(hunters));

		scn.MoveCompanionsToTable(legolas);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(hunters));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileGimliIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var dwarf = scn.GetFreepsCard("dwarf");
		scn.MoveCardsToSupportArea(hunters);
		scn.MoveCompanionsToTable(gimli, legolas, dwarf);

		scn.StartGame();
		// Gimli exhausted (vitality 3, 2 wounds); another Dwarf companion beside him could exert.
		scn.AddWoundsToChar(gimli, 2);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(dwarf));
		assertFalse(scn.FreepsActionAvailable(hunters));

		scn.RemoveWoundsFromChar(gimli, 2);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(hunters));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
