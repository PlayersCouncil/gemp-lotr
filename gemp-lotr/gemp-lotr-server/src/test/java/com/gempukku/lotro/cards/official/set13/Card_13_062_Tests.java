package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_062_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "13_62");
					put("faramir", "12_47");    // Faramir, Dúnadan of Gondor: [gondor] companion, strength 7, resistance 7; a skirmish ability only
					put("armor", "1_92");       // Armor: [gondor] armor possession on a Man; a cap on skirmish wounds only
					put("shield", "1_107");     // Great Shield: [gondor] shield possession on a Man; the minion archery total -1
					put("mail", "1_101");       // Coat of Mail: [gondor] armor possession on a Man; no overwhelm unless tripled
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: a [gondor] ARTIFACT, vitality +1; a response used only on purpose
					put("saga", "1_114");       // The Saga of Elendil: a [gondor] condition a companion bears, vitality +1; a skirmish ability used only on purpose
					put("prowling", "11_136");  // Prowling Orc: [orc] Orc lurker, cost 3, site 4; a skirmish ability used only on purpose
					put("column", "11_72");     // Column of Easterlings: [men] Man lurker, cost 3, site 4; a skirmish ability used only on purpose
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, no lurker, strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoromirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Boromir, Doomed Heir
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Boromir is resistance -1 for each lurker minion you can spot.<br><b>Skirmish:</b> Exert Boromir to make him strength +1 for each possession he bears.
		*/

		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");

		assertEquals("Boromir", boromir.getBlueprint().getTitle());
		assertEquals("Doomed Heir", boromir.getBlueprint().getSubtitle());
		assertTrue(boromir.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, boromir.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, boromir.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, boromir.getBlueprint().getCardType());
		assertEquals(Race.MAN, boromir.getBlueprint().getRace());
		assertEquals(3, boromir.getBlueprint().getTwilightCost());
		assertEquals(8, boromir.getBlueprint().getStrength());
		assertEquals(3, boromir.getBlueprint().getVitality());
		assertEquals(6, boromir.getBlueprint().getResistance());
	}

	@Test
	public void BoromirIsResistanceMinusOneForEachLurkerMinionPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var faramir = scn.GetFreepsCard("faramir");
		var prowling = scn.GetShadowCard("prowling");
		var column = scn.GetShadowCard("column");
		scn.MoveCompanionsToTable(boromir, faramir);
		scn.MoveCardsToHand(prowling, column);

		scn.StartGame();
		assertInZone(Zone.HAND, prowling, column);
		scn.SetTwilight(10);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);
		scn.FreepsPass();

		// A minion that is no lurker counts for nothing.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(6, scn.GetResistance(boromir));
		assertEquals(7, scn.GetResistance(faramir));

		scn.ShadowPlayCard(prowling);
		assertEquals(5, scn.GetResistance(boromir));

		// A second lurker, of another culture and race.
		scn.ShadowPlayCard(column);
		assertEquals(4, scn.GetResistance(boromir));
		// Boromir's alone.
		assertEquals(7, scn.GetResistance(faramir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsHimToMakeHimStrengthPlusOneForEachPossessionHeBearsForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var faramir = scn.GetFreepsCard("faramir");
		var armor = scn.GetFreepsCard("armor");
		var shield = scn.GetFreepsCard("shield");
		var mail = scn.GetFreepsCard("mail");
		var narsil = scn.GetFreepsCard("narsil");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(boromir, faramir);
		scn.AttachCardsTo(boromir, armor, shield, narsil, saga);
		scn.AttachCardsTo(faramir, mail);

		scn.StartGame();
		assertAttachedTo(armor, boromir);
		assertAttachedTo(shield, boromir);
		assertAttachedTo(narsil, boromir);
		assertAttachedTo(saga, boromir);
		assertAttachedTo(mail, faramir);
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, runner1},
				new PhysicalCardImpl[]{faramir, runner2});
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(boromir));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(boromir));
		scn.FreepsUseCardAction(boromir);

		assertEquals(1, scn.GetWoundsOn(boromir));
		// 8 + 1 each for the Armor and the Great Shield; not for Narsil (an artifact), the Saga (a condition) or
		// the Coat of Mail Faramir bears.
		assertEquals(10, scn.GetStrength(boromir));
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: Boromir is 8 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(boromir));
	}

	@Test
	public void UsedOutOfHisOwnSkirmishItCountsWhatHeBearsFromNoneAndIsNotOfferedOnceHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var faramir = scn.GetFreepsCard("faramir");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(boromir, faramir);

		scn.StartGame();
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, runner1);
		scn.FreepsResolveSkirmish(faramir);

		// Boromir is in no skirmish and bears nothing: the ability is offered and its exertion buys nothing.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(boromir));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(boromir));
		scn.FreepsUseCardAction(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(8, scn.GetStrength(boromir));

		scn.ShadowPass();
		// With one possession borne, +1.
		scn.AttachCardsTo(boromir, armor);
		assertAttachedTo(armor, boromir);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(boromir));
		scn.FreepsUseCardAction(boromir);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(9, scn.GetStrength(boromir));

		// Vitality 3 and two wounds: exhausted, he can no longer pay.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(boromir));
	}
}
