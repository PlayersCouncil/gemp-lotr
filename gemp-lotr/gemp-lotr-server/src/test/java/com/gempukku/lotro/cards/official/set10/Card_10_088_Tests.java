package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gothmog", "10_88");

					put("garrison", "7_273");      // Gorgoroth Garrison: besieger Orc, not fierce; two Regroup abilities only
					put("mordorpillager", "10_92"); // Mordor Pillager: "Shadow: Exert this minion and spot another [sauron] minion to control a site"

					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor] Man, a Maneuver ability only
					put("gimli", "1_13");          // Gimli, Son of Gloin: Dwarf, a Skirmish ability only
					put("boromir", "1_97");        // Boromir, Son of Denethor: vitality 3, a Skirmish ability only
					put("barliman", "1_70");       // Barliman Butterbur: a [gandalf] ally, no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GothmogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Gothmog, Lieutenant of Morgul
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Besieger</b>.<br>When you play Gothmog, the Free Peoples player must wound a companion for each site you control.<br>While you control a site, Gothmog is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gothmog");

		assertEquals("Gothmog", card.getBlueprint().getTitle());
		assertEquals("Lieutenant of Morgul", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithOneSiteControlledTheFreePeoplesPlayerWoundsOneCompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveAlliesToTable(barliman);

		var gothmog = scn.GetShadowCard("gothmog");
		var garrison = scn.GetShadowCard("garrison");
		scn.MoveCardsToHand(gothmog);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		// Boromir, vitality 3, is exhausted: a wound kills him where an exertion could not be paid.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(garrison);
		scn.FreepsPass();

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.ShadowPlayCard(gothmog);

		// The Free Peoples player chooses; every companion is offered, the exhausted one too, and no ally or minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, garrison, gothmog));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);

		assertInZone(Zone.DEAD, boromir);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoSitesControlledTwoWoundsArePlacedAndOneCompanionMayTakeBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var gothmog = scn.GetShadowCard("gothmog");
		scn.MoveCardsToHand(gothmog);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		scn.FreepsPass();

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(gothmog);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// "For each" lets one companion be wounded again (CR 4.2 "for each").
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileOnlyTheFreePeoplesPlayerControlsASiteNoWoundIsPlacedAndGothmogIsNotFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var gothmog = scn.GetShadowCard("gothmog");
		scn.MoveCardsToHand(gothmog);

		scn.StartGame();
		scn.SkipToSite(2);
		// Player 1 took site 1 on player 2's turn.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		scn.FreepsPass();

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(gothmog);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(gothmog, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionPlayedOnceGothmogIsInPlayWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var gothmog = scn.GetShadowCard("gothmog");
		var garrison = scn.GetShadowCard("garrison");
		scn.MoveCardsToHand(garrison);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(gothmog);
		scn.FreepsPass();

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		// A besieger Orc as Gothmog is: played after him, it sets off nothing of his.
		scn.ShadowPlayCard(garrison);

		assertInZone(Zone.SHADOW_CHARACTERS, garrison);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void GothmogBecomesFierceAsASiteIsTakenAndNoOtherMinionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Three Free Peoples cultures ([shire], [gondor], [dwarven]): the Mordor Pillager's site is not preventable.
		scn.MoveCompanionsToTable("aragorn", "gimli");

		var gothmog = scn.GetShadowCard("gothmog");
		var garrison = scn.GetShadowCard("garrison");
		var mordorpillager = scn.GetShadowCard("mordorpillager");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(gothmog, garrison, mordorpillager);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertFalse(scn.ShadowControls(site1));
		assertFalse(scn.HasKeyword(gothmog, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(garrison, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(mordorpillager));

		scn.ShadowUseCardAction(mordorpillager);

		assertEquals(1, scn.GetWoundsOn(mordorpillager));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.HasKeyword(gothmog, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(garrison, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
