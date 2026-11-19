package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ransacker", "17_52");
					put("ransacker2", "17_52");
					put("warrior", "17_60");    // Sunland Warrior: [men] Man, cost 3; a maneuver ability only
					// Easterling Runner: [men] Man of another title; a Shadow ability that needs a culture token, never used
					put("runner", "13_90");
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard); his texts are not reached here
					put("saruman", "17_48");
					put("brigand", "4_10");     // Dunlending Brigand: a Man of [dunland], no text
					// Beast of War: [men] mount on a [men] Man; bearer is fierce while a threat can be spotted, damage +1
					// from 3 threats
					put("beast", "17_40");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound, strength 8, vitality 4; a maneuver ability never used
					put("gimli", "1_13");       // Gimli, Son of Glóin: unbound, strength 6, vitality 3; a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: unbound, strength 7, vitality 3; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StampedingRansackerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Stampeding Ransacker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot 2 [men] Men.<br>Each time this minion wins a skirmish and you can spot a site you control, exhaust an unbound companion.
		*/

		var scn = GetScenario();

		var ransacker = scn.GetShadowCard("ransacker");

		assertEquals("Stampeding Ransacker", ransacker.getBlueprint().getTitle());
		assertNull(ransacker.getBlueprint().getSubtitle());
		assertFalse(ransacker.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, ransacker.getBlueprint().getSide());
		assertEquals(Culture.MEN, ransacker.getBlueprint().getCulture());
		assertEquals(CardType.MINION, ransacker.getBlueprint().getCardType());
		assertEquals(Race.MAN, ransacker.getBlueprint().getRace());
		assertEquals(6, ransacker.getBlueprint().getTwilightCost());
		assertEquals(13, ransacker.getBlueprint().getStrength());
		assertEquals(3, ransacker.getBlueprint().getVitality());
		assertEquals(4, ransacker.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedWithOneMenManBesideAMenWizardAndADunlandManAndCanWithTwoMenMen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var warrior = scn.GetShadowCard("warrior");
		var runner = scn.GetShadowCard("runner");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(ransacker);
		assertInHand(ransacker);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, saruman, brigand);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertFalse(scn.ShadowPlayAvailable(ransacker));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second [men] Man, of another title.
		scn.MoveMinionsToTable(runner);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(ransacker));
		scn.ShadowPlayCard(ransacker);

		assertInZone(Zone.SHADOW_CHARACTERS, ransacker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WinningASkirmishWithASiteControlledExhaustsAnUnboundCompanionOfTheShadowPlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var ransacker2 = scn.GetShadowCard("ransacker2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(ransacker, ransacker2);
		assertInZone(Zone.SHADOW_CHARACTERS, ransacker, ransacker2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, ransacker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		// Strength 13 against 7: a win, and no overwhelm.
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(boromir));
		// The Shadow player chooses among the unbound companions, the loser among them; not the Ring-bearer, a
		// Ring-bound companion, an ally or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, bounder, ransacker, ransacker2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// Vitality 4: exhausted with 3 wounds.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));
		// The unassigned copy won nothing and asks nothing.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishExhaustsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(ransacker);
		// Aragorn 8 + 6 = 14 beats the Ransacker's 13; the Ransacker, vitality 3, lives through the loss.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, ransacker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(ransacker));
		assertInZone(Zone.SHADOW_CHARACTERS, ransacker);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningWithOnlyTheFreePeoplesPlayerControllingASiteExhaustsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(ransacker);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, ransacker);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// Only the rule's wound for losing.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningTheNormalAndTheFierceSkirmishExhaustsAnUnboundCompanionEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var beast = scn.GetShadowCard("beast");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(ransacker);
		scn.AttachCardsTo(ransacker, beast);
		scn.AddThreats(1);
		assertInZone(Zone.ATTACHED, beast);
		assertEquals(1, scn.GetThreats());
		// Boromir 7 + 2 = 9 against 13 + 3 = 16: he loses and is not overwhelmed.
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 2));

		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(ransacker, Keyword.FIERCE));
		scn.FreepsAssignToMinions(boromir, ransacker);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, boromir));
		scn.ShadowChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(boromir));

		// The fierce skirmish: a second win, a second exhaustion.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, ransacker);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// Aragorn and now Boromir are exhausted, so Gimli, the one left, is exhausted unasked.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
