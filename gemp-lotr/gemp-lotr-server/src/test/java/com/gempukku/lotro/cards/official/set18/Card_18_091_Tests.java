package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sneak", "18_91");

					put("goblin", "11_137");    // Scurrying Goblin: [orc] Orc; its only text answers its own play at an underground site
					put("halftroll", "14_12");  // Half-troll of Far Harad: [orc] Half-troll, no Orc; a start-of-assignment offer that needs more companions than minions
					put("trooper", "3_99");     // Orc Trooper: [sauron] Orc; strength +1 for each card in hand

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound; a maneuver ability used only on purpose
					put("smeagol", "9_30");     // Smeagol, Bearer of Great Secrets: [gollum] Ring-bound companion, not the Ring-bearer here
					put("visitor", "7_126");    // Unexpected Visitor: on a [gondor] companion, each minion gains "Assignment: Assign this minion to bearer of Unexpected Visitor"
					put("eowyn", "17_96");      // Eowyn, Northwoman: "Assignment: Exert Eowyn and assign a minion to the Ring-bearer ..."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,   // Galadriel, Bearer of Wisdom: an [elven] Elf, vitality 3
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sneak", "18_91");
					put("denizen", "11_115");   // Denizen of Khazad-dum: [orc] Orc; strength +2 at an underground site only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,       // Gimli, Bearer of Grudges: a [dwarven] Dwarf; his text acts only in his own skirmish
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFrodoScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sneak", "18_91");
					put("goblin", "11_137");    // Scurrying Goblin: [orc] Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // a [shire] Ring-bearer
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishSneakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Orkish Sneak
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Assignment:</b> Spot another [orc] Orc to assign this minion to skirmish a non-[shire] Ring-bearer.
		 * 	The Free Peoples player may add a burden or exert the Ring-bearer twice to discard this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sneak");

		assertEquals("Orkish Sneak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignsItselfToTheNonShireRingBearerAndTheFreePeoplesPlayerMayDecline() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(aragorn);

		var sneak = scn.GetShadowCard("sneak");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, goblin);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Smeagol comes in after the move, where his own move trigger would have taken Aragorn.
		scn.MoveCompanionsToTable(smeagol);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(sneak));
		scn.ShadowUseCardAction(sneak);

		// The Ring-bearer is the only one he can go to: neither Smeagol (Ring-bound, not the Ring-bearer) nor Aragorn.
		assertTrue(scn.IsCharAssignedAgainst(galadriel, sneak));
		assertFalse(scn.IsCharAssigned(smeagol));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(goblin));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a burden or exert the Ring-bearer twice"));
		scn.FreepsChooseNo();

		assertInPlay(sneak, goblin);
		assertTrue(scn.IsCharAssignedAgainst(galadriel, sneak));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheRingBearerExertingTwiceDiscardsHimAndUndoesTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(aragorn);

		var sneak = scn.GetShadowCard("sneak");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, goblin);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Smeagol comes in after the move, where his own move trigger would have taken Aragorn.
		scn.MoveCompanionsToTable(smeagol);
		scn.FreepsPass();

		scn.ShadowUseCardAction(sneak);
		assertTrue(scn.IsCharAssignedAgainst(galadriel, sneak));
		scn.FreepsChooseYes();

		// Both can be paid: he is asked which.
		assertEquals(List.of("Add a burden", "Exert the Ring-bearer twice"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Exert the Ring-bearer twice");

		// Smeagol is Ring-bound as well, and is not asked about: the exertion is the Ring-bearer's.
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(sneak);
		assertFalse(scn.IsCharAssigned(galadriel));
		// The [orc] Orc spotted is not the minion discarded.
		assertInPlay(goblin);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ABurdenDiscardsHimFromADwarfRingBearerSpottingAnotherOrcTitle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var gimli = scn.GetRingBearer();

		var sneak = scn.GetShadowCard("sneak");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, denizen);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(sneak));
		scn.ShadowUseCardAction(sneak);
		assertTrue(scn.IsCharAssignedAgainst(gimli, sneak));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsChooseYes();
		scn.FreepsChoose("Add a burden");

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInDiscard(sneak);
		assertFalse(scn.IsCharAssigned(gimli));
		assertInPlay(denizen);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ARingBearerWhoCannotExertTwicePaysWithTheBurdenUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetRingBearer();

		var sneak = scn.GetShadowCard("sneak");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		// Vitality 3 with 1 wound: 2 left, and exerting twice needs 3.
		scn.AddWoundsToChar(galadriel, 1);
		scn.MoveMinionsToTable(sneak, goblin);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(sneak);
		assertTrue(scn.IsCharAssignedAgainst(galadriel, sneak));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a burden or exert the Ring-bearer twice"));
		scn.FreepsChooseYes();

		// Only the burden can be paid in full, so it is paid with nothing more asked.
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertInDiscard(sneak);
		assertFalse(scn.IsCharAssigned(galadriel));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedUntilAnotherOrcCultureOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetRingBearer();

		var sneak = scn.GetShadowCard("sneak");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var trooper = scn.GetShadowCard("trooper");

		scn.StartGame();
		// Beside him, an [orc] minion who is no Orc and an Orc who is not [orc].
		scn.MoveMinionsToTable(sneak, halftroll, trooper);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sneak));

		scn.MoveMinionsToTable(goblin);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(sneak));
		scn.ShadowUseCardAction(sneak);
		assertTrue(scn.IsCharAssignedAgainst(galadriel, sneak));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a burden or exert the Ring-bearer twice"));
	}

	@Test
	public void NotOfferedWithAShireRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoScenario();

		var frodo = scn.GetRingBearer();

		var sneak = scn.GetShadowCard("sneak");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, goblin);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sneak));
		assertFalse(scn.IsCharAssigned(frodo));
	}

	@Test
	public void NotOfferedOnceHeIsAssignedElsewhere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		var sneak = scn.GetShadowCard("sneak");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, goblin);
		assertAttachedTo(visitor, aragorn);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The ability Unexpected Visitor gives him puts him on Aragorn first.
		scn.ShadowUseCardAction(sneak, "Visitor");
		assertTrue(scn.IsCharAssignedAgainst(aragorn, sneak));
		scn.FreepsPass();

		// The Ring-bearer is still unassigned, but he no longer is.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(galadriel));
		assertFalse(scn.ShadowActionAvailable(sneak));
	}

	@Test
	public void NotOfferedOnceTheRingBearerIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);

		var sneak = scn.GetShadowCard("sneak");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, goblin);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Eowyn assigns the Scurrying Goblin to the Ring-bearer.
		scn.FreepsUseCardAction(eowyn);
		scn.FreepsChooseCard(goblin);
		assertTrue(scn.IsCharAssignedAgainst(galadriel, goblin));

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(sneak));
		assertFalse(scn.ShadowActionAvailable(sneak));
	}
}
