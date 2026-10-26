package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_19_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "19_39");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFierceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "19_39");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used
					// Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability never used
					put("boromir", "1_97");
					// Black Rider: a fierce [wraith] Nazgûl, not unique; strength 8, +1 for each companion
					put("rider", "12_161");
					// Thrarin, Dwarven Smith: an ally; "Maneuver: Exert Thrarin to allow him to participate in
					// archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireToldeaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Úlairë Toldëa, Dark Shadow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>Úlairë Toldëa cannot be assigned to skirmish the same companion in a fierce skirmish that he has already been assigned to this turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toldea");

		assertEquals("Úlairë Toldëa", card.getBlueprint().getTitle());
		assertEquals("Dark Shadow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void InTheFierceRoundToldeaCannotBeAssignedToTheCompanionHeWasAssignedToEarlierThisTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFierceScenario();

		var toldea = scn.GetShadowCard("toldea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		scn.SkipToAssignments();

		// The first round: either player may assign him to either companion.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, toldea));
		assertTrue(scn.ShadowCanAssignAgainst(aragorn, toldea));
		assertTrue(scn.FreepsCanAssignAgainst(boromir, toldea));
		assertTrue(scn.ShadowCanAssignAgainst(boromir, toldea));

		// Aragorn's 8 loses to 12, and he takes a wound.
		scn.FreepsAssignAndResolve(aragorn, toldea);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(toldea));

		// The fierce round. He was assigned to Aragorn this turn and to Boromir he was not: neither player
		// may give him Aragorn again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, toldea));
		assertFalse(scn.ShadowCanAssignAgainst(aragorn, toldea));
		assertTrue(scn.FreepsCanAssignAgainst(boromir, toldea));
		assertTrue(scn.ShadowCanAssignAgainst(boromir, toldea));

		scn.FreepsAssignToMinions(boromir, toldea);
		assertTrue(scn.IsCharAssignedAgainst(boromir, toldea));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnotherFierceMinionMayBeAssignedAgainToTheCompanionItSkirmishedEarlierThisTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFierceScenario();

		var toldea = scn.GetShadowCard("toldea");
		var rider = scn.GetShadowCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(toldea, rider);
		scn.SkipToAssignments();

		// Black Rider is 8 + 3 for the three companions
		assertEquals(11, scn.GetStrength(rider));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, toldea}, new PhysicalCardImpl[]{boromir, rider});
		assertTrue(scn.IsCharAssignedAgainst(aragorn, toldea));
		assertTrue(scn.IsCharAssignedAgainst(boromir, rider));

		// Aragorn's 8 loses to 12 and Boromir's 7 to 11: a wound each, and both minions survive
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		// The Black Rider may have Boromir again; Toldëa may not have Aragorn
		assertTrue(scn.FreepsCanAssignAgainst(boromir, rider));
		assertTrue(scn.ShadowCanAssignAgainst(boromir, rider));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, toldea));
		assertFalse(scn.ShadowCanAssignAgainst(aragorn, toldea));

		scn.FreepsAssignToMinions(boromir, rider);
		assertTrue(scn.IsCharAssignedAgainst(boromir, rider));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.ShadowCanAssignAgainst(aragorn, toldea));
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(toldea));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnAllyHeSkirmishedMayBeAssignedToHimAgainInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFierceScenario();

		var toldea = scn.GetShadowCard("toldea");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// Numbers that only keep Thrarin alive: strength 8 is not overwhelmed by 12, and vitality 3 lives through
		// his own exertion and a lost skirmish
		scn.ApplyAdHocModifier(new StrengthModifier(null, thrarin, null, 4));
		scn.ApplyAdHocModifier(new VitalityModifier(null, thrarin, null, 1));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();

		assertTrue(scn.FreepsCanAssignAgainst(thrarin, toldea));
		scn.FreepsAssignToMinions(thrarin, toldea);
		assertTrue(scn.IsCharAssignedAgainst(thrarin, toldea));
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(thrarin));

		// Thrarin is no companion: having been assigned to Toldëa this turn does not keep him from him again
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsCanAssignAgainst(thrarin, toldea));
		assertTrue(scn.ShadowCanAssignAgainst(thrarin, toldea));

		scn.FreepsAssignToMinions(thrarin, toldea);
		assertTrue(scn.IsCharAssignedAgainst(thrarin, toldea));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
