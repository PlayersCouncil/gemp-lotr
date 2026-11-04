package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fredegar", "18_107");
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit, strength 3 (cheated in); an assignment ability only
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3; +2 only while he bears a weapon
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, strength 2; a skirmish ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3 (cheated in); a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FredegarBolgerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Fredegar Bolger, Fatty
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While the fellowship is in region 1 and you can spot 2 other [shire] companions, Fredegar Bolger is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fredegar");

		assertEquals("Fredegar Bolger", card.getBlueprint().getTitle());
		assertEquals("Fatty", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void InRegionOneWithTwoOtherShireCompanionsOfAnyRaceFredegarIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fredegar = scn.GetFreepsCard("fredegar");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		// Frodo, a Hobbit, and Smeagol, a [shire] companion who is none.
		scn.MoveCompanionsToTable(fredegar, smeagol);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		// 3 + 3
		assertEquals(6, scn.GetStrength(fredegar));
		// The others are untouched: Frodo 3 + 1 for The Ruling Ring, Smeagol 3.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBonusComesOnAsTheSecondOtherShireCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fredegar = scn.GetFreepsCard("fredegar");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(fredegar);
		scn.MoveCardsToHand(merry);
		assertInHand(merry);

		scn.StartGame();

		// Frodo is the one other [shire] companion.
		assertEquals(3, scn.GetStrength(fredegar));
		assertTrue(scn.FreepsPlayAvailable(merry));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		// 3 + 3, with Frodo and Merry spotted; neither of them is made stronger.
		assertEquals(6, scn.GetStrength(fredegar));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOneOtherShireCompanionBesideAShireAllyAndAGondorHobbitFredegarIsNotStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fredegar = scn.GetFreepsCard("fredegar");
		var frodo = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");
		var pippin = scn.GetFreepsCard("pippin");
		// Frodo is the one other [shire] companion.  Fredegar is one himself, but not "other"; Bounder is [shire]
		// and no companion; Pippin is a Hobbit companion of the [gondor] culture.  Any one of them counted makes 2.
		scn.MoveCompanionsToTable(fredegar, pippin);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetStrength(fredegar));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OutsideRegionOneFredegarIsNotStrongerThoughTheOtherFellowshipIsStillThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fredegar = scn.GetFreepsCard("fredegar");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(fredegar, merry);

		scn.StartGame();
		scn.SkipToSite(3);

		// Site 3, the last of region 1, with Frodo and Merry beside him.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(fredegar));

		// The fellowship moves on to site 4, in region 2; the other player's marker is still at site 3.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetStrength(fredegar));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
