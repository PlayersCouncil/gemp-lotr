package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "6_88");
					// Úlairë Lemenya, Lieutenant of Morgul: a Nazgûl, strength 9, vitality 2.  "Each companion or ally
					// who bears a [wraith] condition is strength -2": nobody here bears one.
					put("lemenya", "1_232");

					// Aragorn and Boromir are strong enough that a strength of 12 does not overwhelm them.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability used only on purpose
					// Elrond, Lord of Rivendell: an ally, vitality 4.  His start-of-turn heal finds nobody wounded,
					// and his fellowship ability is never used.
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireToldeaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Úlairë Toldëa, Winged Sentry
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Fierce.
		 * 	Each time Úlairë Toldëa wins a skirmish, the Free Peoples player must exert a companion or add a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toldea");

		assertEquals("Úlairë Toldëa", card.getBlueprint().getTitle());
		assertEquals("Winged Sentry", card.getBlueprint().getSubtitle());
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
	public void EachTimeSheWinsTheFreePeoplesPlayerExertsACompanionOrAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(elrond);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, toldea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 12 against 8: she wins.  Her trigger answers the win before the wound for losing.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Free Peoples player is the one who chooses.
		assertEquals(List.of("Exert a companion", "Add a burden"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Exert a companion");

		// Any companion, the Ring-bearer and the loser included; not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond, toldea));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetBurdens());

		// She is fierce, and wins again: asked a second time.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, toldea);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertEquals(List.of("Exert a companion", "Add a burden"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a burden");

		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoCompanionAbleToExertABurdenIsAddedWithoutAQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// Frodo and Aragorn are exhausted.  Elrond could exert, but is no companion.  (Her win is answered before
		// Aragorn's wound for losing, engine pass 54; until then that wound was what exhausted him.)
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, toldea);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();

		// The burden, with nothing asked; then the wound for losing kills Aragorn.
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.FreepsDecisionAvailable("Exert a companion"));
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnotherNazgulWinningASkirmishDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var toldea = scn.GetShadowCard("toldea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea, lemenya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, lemenya }, new PhysicalCardImpl[] { boromir, toldea });
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 9 against 8: Lemenya wins.  Aragorn takes his wound and nothing more is asked or done.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.FreepsDecisionAvailable("Exert a companion"));

		// Her own skirmish, which she wins: now it is asked.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(List.of("Exert a companion", "Add a burden"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
