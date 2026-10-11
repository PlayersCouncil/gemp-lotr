package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anduril", "7_80");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], strength 8; a maneuver ability used only on purpose
					put("rohan", "17_93");      // Aragorn, Defender of Rohan: the Aragorn who is not [gondor], strength 8
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man of another title, strength 7; a skirmish ability
					put("sting", "18_113");     // Sting, Elven Long Knife: on Frodo, vitality +1; "Regroup: Exert bearer to remove a threat."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AndurilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Andúril, King's Blade
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be Aragorn.<br>If you cannot spot a threat, Aragorn is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("anduril");

		assertEquals("Andúril", card.getBlueprint().getTitle());
		assertEquals("King's Blade", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAragornForThreeAndNotOnBoromir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(anduril);
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(anduril));

		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(anduril));
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		scn.FreepsPlayCard(anduril);

		// Aragorn is the only bearer there can be, so no choice of bearer is asked.
		assertEquals(3, scn.GetTwilight());
		assertEquals(aragorn, anduril.getAttachedTo());
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		// No threat can be spotted: he is defender +1 as soon as he bears it, and Boromir beside him is not.
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnAnAragornWhoIsNotGondor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var rohan = scn.GetFreepsCard("rohan");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(anduril);
		scn.MoveCompanionsToTable(rohan, boromir);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(anduril));
		assertEquals(8, scn.GetStrength(rohan));
		assertEquals(0, scn.GetKeywordCount(rohan, Keyword.DEFENDER));

		scn.FreepsPlayCard(anduril);

		assertEquals(3, scn.GetTwilight());
		assertEquals(rohan, anduril.getAttachedTo());
		assertEquals(10, scn.GetStrength(rohan));
		assertEquals(1, scn.GetKeywordCount(rohan, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AragornIsDefenderPlus1OnlyWhileNoThreatCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));

		// One threat is enough to take it away.
		scn.AddThreats(1);

		assertEquals(1, scn.GetThreats());
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
	}

	@Test
	public void RemovingTheLastThreatMakesAragornDefenderPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);
		scn.AttachCardsTo(frodo, sting);

		scn.StartGame();
		// Two companions, so two threats is as many as there can be.
		scn.AddThreats(2);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		scn.FreepsUseCardAction(sting);

		// One threat is still there to be spotted.
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		scn.ShadowPass();
		scn.FreepsUseCardAction(sting);

		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
