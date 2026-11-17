package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("madman", "17_51");
					put("madman2", "17_51");
					put("warrior", "17_60");    // Sunland Warrior: [men] Man, cost 3; a maneuver ability only
					// Easterling Runner: [men] Man of another title; a Shadow ability that needs a culture token, never used
					put("runner", "13_90");
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard); his texts are not reached here
					put("saruman", "17_48");
					put("brigand", "4_10");     // Dunlending Brigand: a Man of [dunland], no text
					put("hillsman", "17_50");   // Stampeding Hillsman: [men] Man, cost 2; a regroup ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: companion, vitality 3; a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: companion, vitality 3; a skirmish ability only
					put("bounder", "1_286");    // Bounder: Hobbit ally; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StampedingMadmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Stampeding Madman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot 2 [men] Men.<br>When you play this minion, you may make the Free Peoples player exert a companion for each site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("madman");

		assertEquals("Stampeding Madman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedWithOneMenManBesideAMenWizardAndADunlandManAndCanWithTwoMenMen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madman = scn.GetShadowCard("madman");
		var warrior = scn.GetShadowCard("warrior");
		var runner = scn.GetShadowCard("runner");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(madman);
		assertInHand(madman);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(warrior, saruman, brigand);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertFalse(scn.ShadowPlayAvailable(madman));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second [men] Man, of another title.
		scn.MoveMinionsToTable(runner);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(madman));
		scn.ShadowPlayCard(madman);

		assertInZone(Zone.SHADOW_CHARACTERS, madman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(madman));
	}

	@Test
	public void WithNoSiteControlledThePlayOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madman = scn.GetShadowCard("madman");
		var warrior = scn.GetShadowCard("warrior");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(madman);
		scn.MoveCompanionsToTable(gimli);
		assertInHand(madman);

		// No site can be controlled on the first turn.
		scn.StartGame();
		scn.MoveMinionsToTable(warrior, runner);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.ShadowPlayCard(madman);

		assertInZone(Zone.SHADOW_CHARACTERS, madman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoSitesControlledTheFreePeoplesPlayerExertsACompanionTwiceAndMayChooseTheSameOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madman = scn.GetShadowCard("madman");
		var madman2 = scn.GetShadowCard("madman2");
		var warrior = scn.GetShadowCard("warrior");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToHand(madman);
		scn.MoveCompanionsToTable(gimli, boromir);
		scn.MoveAlliesToTable(bounder);
		assertInHand(madman);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		// Boromir exhausted (vitality 3, 2 wounds)
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(warrior, madman2);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.ShadowPlayCard(madman);

		// The copy already in play makes no offer of its own.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(madman));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(madman2));
		scn.ShadowAcceptOptionalTrigger();

		// The Free Peoples player chooses: companions who can exert, not the exhausted one, the ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, bounder, warrior));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// The second exertion may fall on the same companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli));
		scn.FreepsChooseCard(gimli);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneSiteControlledOneCompanionExertsAndAnotherMenManPlayedAfterSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madman = scn.GetShadowCard("madman");
		var hillsman = scn.GetShadowCard("hillsman");
		var warrior = scn.GetShadowCard("warrior");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(madman, hillsman);
		scn.MoveCompanionsToTable(gimli);
		assertInHand(madman, hillsman);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertFalse(scn.ShadowControls(scn.GetSite(2)));
		scn.MoveMinionsToTable(warrior, runner);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.ShadowPlayCard(madman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(madman));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseCard(gimli);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(hillsman);
		assertInZone(Zone.SHADOW_CHARACTERS, hillsman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madman = scn.GetShadowCard("madman");
		var warrior = scn.GetShadowCard("warrior");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(madman);
		scn.MoveCompanionsToTable(gimli);
		assertInHand(madman);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(warrior, runner);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.ShadowPlayCard(madman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(madman));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SHADOW_CHARACTERS, madman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
