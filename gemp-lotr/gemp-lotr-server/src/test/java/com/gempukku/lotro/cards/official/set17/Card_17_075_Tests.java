package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dreg", "17_75");
					put("dreg2", "17_75");
					put("denizen", "11_115");   // Denizen of Khazad-dûm: [orc] Orc; strength +2 only at an underground site
					put("halftroll", "14_12");  // Half-troll of Far Harad: an [orc] minion who is no Orc; a start-of-assignment trigger only
					put("brute", "10_90");      // Mordor Brute: a [sauron] Orc; a wound bar for Orcs bearing possessions
					put("shadow", "12_83");     // The Beckoning Shadow: an [orc] condition, no minion; nothing below 5 burdens
					put("gollum", "5_25");      // Gollum, Stinker: a [gollum] Shadow card, which shows no Free Peoples culture
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] companion; a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] companion, vitality 3; a skirmish ability only
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability only
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession borne by a companion; the minion archery total -1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishDregStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Dreg
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time you play this minion, you may spot another [orc] minion to exert a companion for each Free Peoples culture you can spot over 2.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("dreg");

		assertEquals("Orkish Dreg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithThreeFreePeoplesCulturesTheShadowPlayerExertsOneCompanionWhoCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dreg = scn.GetShadowCard("dreg");
		var denizen = scn.GetShadowCard("denizen");
		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToHand(dreg);
		// Three Free Peoples cultures: [shire] (Frodo, Bounder), [gondor] (Aragorn, Boromir) and [dwarven] (Gimli).
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveAlliesToTable(bounder);
		assertInHand(dreg);

		scn.StartGame();
		// Boromir exhausted (vitality 3, 2 wounds).
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		// Gollum is a [gollum] Shadow card: if he were counted as a culture there would be two exertions.
		scn.MoveMinionsToTable(denizen, gollum);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.ShadowPlayCard(dreg);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dreg));
		scn.ShadowAcceptOptionalTrigger();

		// The Shadow player chooses: companions who can exert; not the exhausted one, the ally or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, bounder, denizen, dreg));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		// One culture over 2: one exertion, and nothing more is asked.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFourCulturesOneShownByAPossessionTwoExertionsMayFallOnTheSameCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dreg = scn.GetShadowCard("dreg");
		var halftroll = scn.GetShadowCard("halftroll");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCardsToHand(dreg);
		scn.MoveCompanionsToTable(aragorn, gimli);
		// [shire], [gondor], [dwarven], and [elven] by the Cloak alone: two over 2.
		scn.AttachCardsTo(aragorn, cloak);
		assertInHand(dreg);
		assertAttachedTo(cloak, aragorn);

		scn.StartGame();
		// The only other [orc] minion is no Orc.
		scn.MoveMinionsToTable(halftroll);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.ShadowPlayCard(dreg);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dreg));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.ShadowChooseCard(aragorn);
		// The second exertion may fall on the same companion (CR 4.2 "for each").
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.ShadowChooseCard(aragorn);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedBesideASauronOrcAndAnOrcConditionAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dreg = scn.GetShadowCard("dreg");
		var brute = scn.GetShadowCard("brute");
		var shadow = scn.GetShadowCard("shadow");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(dreg);
		scn.MoveCompanionsToTable(aragorn, gimli);
		assertInHand(dreg);

		scn.StartGame();
		// Three cultures, so the exertion would be owed; but no OTHER [orc] minion is in play,
		// and the Dreg itself, an [orc] minion, is not "another".
		scn.MoveMinionsToTable(brute);
		scn.MoveCardsToSupportArea(shadow);
		assertInZone(Zone.SUPPORT, shadow);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.ShadowPlayCard(dreg);

		assertInZone(Zone.SHADOW_CHARACTERS, dreg);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoCulturesTheExertionsComeToNoneAndNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dreg = scn.GetShadowCard("dreg");
		var denizen = scn.GetShadowCard("denizen");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(dreg);
		// [shire] and [gondor]: none over 2.
		scn.MoveCompanionsToTable(aragorn);
		assertInHand(dreg);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(dreg);

		assertInZone(Zone.SHADOW_CHARACTERS, dreg);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedItExertsNobodyAndOnlyASecondDregPlayedAfterMakesAnotherOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dreg = scn.GetShadowCard("dreg");
		var dreg2 = scn.GetShadowCard("dreg2");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(dreg, dreg2, denizen);
		scn.MoveCompanionsToTable(aragorn, gimli);
		assertInHand(dreg, dreg2, denizen);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll);
		scn.FreepsPass();
		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.ShadowPlayCard(dreg);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dreg));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [orc] minion played after it sets nothing off.
		scn.ShadowPlayCard(denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second Dreg played: its own offer, made once, and taken.
		scn.ShadowPlayCard(dreg2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dreg2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dreg));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(gimli);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
