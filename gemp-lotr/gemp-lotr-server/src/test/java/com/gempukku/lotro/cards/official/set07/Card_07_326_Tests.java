package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_326_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "7_326");
					// Merry, Friend to Sam: a [shire] Hobbit companion, cost 1, strength 3, vitality 4; a skirmish ability used only on purpose
					put("merry", "1_302");
					put("merry2", "1_302");
					// Pippin, Trollslayer: a [gondor] Hobbit companion (to play, spot a [gondor] companion); a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("bounder", "1_286");
					// Smeagol, Once Like Us: a [shire] companion with no race (to play, add a burden)
					put("smeagol", "103_116");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability only
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: strength 3, vitality 4
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "7_326");
					// Frodo, Frenzied Fighter: the one Frodo who cannot bear the Ring, so the one who may be in play
					// beside another Ring-bearer; strength 3, and his texts want more minions than companions, or a
					// skirmish he is not in
					put("frodo", "13_149");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: her only text changes her own resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}


	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Sam, Needer of Vittles
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Signet: Aragorn
		 * Game Text: <b>Ring-bound</b>.<br><b>Fellowship:</b> Play a Hobbit companion to remove a burden.<br><b>Response:</b> If Frodo is killed, make Sam the <b>Ring-bearer (resistance 5)</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sam");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Needer of Vittles", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet()); 
	}

	@Test
	public void FellowshipAbilityPlaysAHobbitCompanionFromHandToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Aragorn is the [gondor] companion Pippin needs to be played.
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(merry, pippin, bounder, smeagol);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertInHand(merry, pippin, bounder, smeagol);
		// Each card in hand can be played: only the wording keeps the ally and the companion who is no Hobbit out.
		assertTrue(scn.FreepsPlayAvailable(merry));
		assertTrue(scn.FreepsPlayAvailable(pippin));
		assertTrue(scn.FreepsPlayAvailable(bounder));
		assertTrue(scn.FreepsPlayAvailable(smeagol));
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.FreepsUseCardAction(sam);
		// Hobbit companions of two cultures; not a Hobbit ally, nor a [shire] companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertInHand(merry, bounder, smeagol);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheOnlyHobbitCompanionInHandCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var merry2 = scn.GetFreepsCard("merry2");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToHand(merry2, bounder, smeagol);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertInHand(merry2, bounder, smeagol);

		// Merry is unique and in play, so the second Merry cannot be played; the ally and Smeagol can, and are no
		// Hobbit companions.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(merry2));
		assertTrue(scn.FreepsPlayAvailable(bounder));
		assertTrue(scn.FreepsPlayAvailable(smeagol));
		assertFalse(scn.FreepsActionAvailable(sam));
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void WhenFrodoIsOverwhelmedSamCanBecomeTheRingBearerWithNoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);

		assertSame(frodo, scn.GetRingBearer());
		assertAttachedTo(ring, frodo);
		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(sam));
		// 5 less 2 burdens
		assertEquals(3, scn.GetResistance(sam));

		// 3 + 1 for the Ring against 13: Frodo is overwhelmed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertAttachedTo(ring, sam);
		// 3 + 1 for the Ring
		assertEquals(4, scn.GetStrength(sam));
		// The burdens went with Frodo.
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(sam));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenFrodoIsKilledByWoundsSamCanBecomeTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertSame(frodo, scn.GetRingBearer());
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Vitality 4: he is killed when the game next checks.
		scn.AddWoundsToChar(frodo, 4);
		assertEquals(4, scn.GetWoundsOn(frodo));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheResponseWhenFrodoIsKilledLosesTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertFalse(scn.GameIsFinished());

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
	}

	@Test
	public void ResponseIsNotOfferedWhenAnotherCompanionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, troop);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// 3 against 13: Merry is overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(frodo, scn.GetRingBearer());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenARingBearerWhoIsNotFrodoIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(galadriel, troop);
		assertFalse(scn.GameIsFinished());

		// 3 + 1 for the Ring against 13: Galadriel is overwhelmed, and Sam may not take her place.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, galadriel);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
	}

	@Test
	public void ResponseIsNotOfferedWhenAFrodoWhoIsNotTheRingBearerIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetFreepsCard("frodo");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, frodo);

		scn.StartGame();

		assertEquals("Frodo", frodo.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertInZone(Zone.FREE_CHARACTERS, frodo);

		// 3 against 13: this Frodo is overwhelmed, but he was not the Ring-bearer.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(galadriel, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
