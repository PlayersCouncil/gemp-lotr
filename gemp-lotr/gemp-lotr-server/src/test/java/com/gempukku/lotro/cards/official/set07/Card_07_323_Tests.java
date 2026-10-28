package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_323_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "7_323");
					// Merry, From O'er the Brandywine: another Hobbit companion, strength 3; his text needs a weapon
					put("merry", "1_303");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a fourth companion; a maneuver ability only
					// Sting, Elven Long Knife: on Frodo; "Regroup: Exert bearer to remove a threat"
					put("sting", "18_113");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Pippin, Sworn to Service
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: While you cannot spot 4 threats, Pippin is strength +1 for each threat you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Sworn to Service", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}

	@Test
	public void StrengthIsPlusOneForEachThreatWhileFewerThanFourCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Four companions, so that four threats can lie.
		scn.MoveCompanionsToTable(pippin, merry, aragorn);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(merry));

		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(4, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(merry));

		scn.AddThreats(2);
		assertEquals(3, scn.GetThreats());
		// 3 + 3 threats
		assertEquals(6, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(merry));

		// Four threats can be spotted: no bonus at all.
		scn.AddThreats(1);
		assertEquals(4, scn.GetThreats());
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(merry));
	}

	@Test
	public void AtFourThreatsTheBonusIsGoneAndComesBackWhenOneIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(pippin, merry, aragorn);
		scn.AttachCardsTo(frodo, sting);
		assertEquals(frodo, sting.getAttachedTo());

		scn.StartGame();
		scn.AddThreats(4);
		assertEquals(4, scn.GetThreats());
		assertEquals(3, scn.GetStrength(pippin));

		// No minion is played, so the turn goes from the move to site 2 straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(4, scn.GetThreats());
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetThreats());
		// 3 + 3 threats
		assertEquals(6, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
