package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "15_56");
					// Aragorn, Heir of Elendil: [gondor] companion, strength 8; defender +1 is his whole text
					put("aragorn", "4_109");
					// Aragorn, Defender of Rohan: the [rohan] Aragorn; his text gives [rohan] companions hunter while
					// the Ring-bearer is assigned, which never happens here
					put("rohanaragorn", "17_93");
					// Boromir, Son of Denethor: a [gondor] Man companion who is no Aragorn; his skirmish ability is
					// never used
					put("boromir", "1_97");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					// Morgul Hound: [wraith] Orc, strength 5, vitality 2; +4 and fierce only at 2 wounds on the
					// Ring-bearer, which never happens here
					put("hound", "7_192");
					// Isengard Forger: [isengard] Orc, strength 6, vitality 2; his Shadow ability is never used
					put("forger", "3_56");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: strength 3, 4 with The Ruling Ring
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornsBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Aragorn's Bow, Ranger's Longbow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Game Text: Bearer must be Aragorn.
		 * He is an <b>archer</b>.
		 * <b>Archery:</b> Add a threat to wound a minion with the lowest strength.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Aragorn's Bow", card.getBlueprint().getTitle());
		assertEquals("Ranger's Longbow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAragornIsInPlayAndThenMakesHimAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bow = scn.GetFreepsCard("bow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(bow, aragorn);

		scn.StartGame();

		// Frodo and Boromir are no Aragorn.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(bow));

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(boromir, Keyword.ARCHER));
		assertTrue(scn.FreepsPlayAvailable(bow));

		// Aragorn is unique, so he is the one bearer it can have and nobody is asked.
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, aragorn);
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(boromir, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(frodo, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnTheRohanAragornAndMakesHimAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(rohanaragorn, boromir);
		scn.MoveCardsToHand(bow);

		scn.StartGame();

		assertFalse(scn.HasKeyword(rohanaragorn, Keyword.ARCHER));
		assertTrue(scn.FreepsPlayAvailable(bow));

		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, rohanaragorn);
		assertTrue(scn.HasKeyword(rohanaragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(boromir, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryAbilityAddsAThreatToWoundAMinionOfTheLowestStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bow = scn.GetFreepsCard("bow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var hound = scn.GetShadowCard("hound");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, hound, forger);
		scn.SkipToPhase(Phase.ARCHERY);

		// Savage and Hound are 5, the Forger 6; Frodo, at 4, is weaker still, but no minion.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(6, scn.GetStrength(forger));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(forger));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(bow);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, hound));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(forger, frodo, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(hound);
		assertEquals(1, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(forger));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void NotOfferedOnceThereAreAsManyThreatsAsCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddThreats(1);
		scn.SkipToPhase(Phase.ARCHERY);

		// Two companions, Frodo and Aragorn: one threat leaves room for another.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
	}
}
