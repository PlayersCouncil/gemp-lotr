package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "11_57");

					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1 for the Merchant, so that he can carry a wound; a response used only on purpose

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("pursuer", "4_190");    // Uruk Pursuer: fierce, strength 5, vitality 3; its other texts are about unbound Hobbits
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoromirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Boromir, Hero of Osgiliath
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: <b>Knight</b>.<br>Each time Boromir wins a skirmish, you may heal a [gondor] companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("boromir");

		assertEquals("Boromir", card.getBlueprint().getTitle());
		assertEquals("Hero of Osgiliath", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void WinningASkirmishOffersAHealOfAWoundedGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(pippin, 2);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(merchant, 1);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merchant));

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// Boromir (7) beats the Savage (5).
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boromir));
		scn.FreepsAcceptOptionalTrigger();

		// The [gondor] companions who are wounded, a Man and a Hobbit, Boromir himself among them; not the
		// [shire] Ring-bearer, nor the [gondor] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		// One wound of his two.
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheHealMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(boromir, pippin);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(pippin, 2);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(pippin));

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boromir));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishOffersNoHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(boromir, pippin);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(pippin, 2);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// The Troop (13) beats Boromir (7) without overwhelming him: one wound, and he lives.  Pippin could be healed.
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(boromir));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoGondorCompanionWoundedTheHealIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Only characters the heal may not reach are wounded.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(merchant, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(pippin));

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(boromir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinOffersTheHealAgainTheFierceSkirmishAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(boromir, pippin);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.AddWoundsToChar(pippin, 3);
		assertEquals(3, scn.GetWoundsOn(pippin));

		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, pursuer);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// Boromir (7) beats the Pursuer (5); Pippin, the only wounded [gondor] companion, is healed unasked.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boromir));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(pippin));

		// The fierce skirmish: Boromir wins a second time.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(boromir, pursuer);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boromir));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
