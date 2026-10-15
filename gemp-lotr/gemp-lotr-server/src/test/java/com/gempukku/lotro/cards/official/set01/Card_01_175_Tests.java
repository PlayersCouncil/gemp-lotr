package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_175_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("domain", "1_175");
					// Goblin Runner: [moria] Orc, cost 1; its play adds (2) unasked
					put("runner", "1_178");
					// Cave Troll of Moria, Scourge of the Black Pit: a [moria] minion who is no Orc (a Troll); cheated
					// into play, so his own "spot a [moria] Orc" is never asked
					put("troll", "1_165");
					// Orc Chieftain: a [sauron] Orc, a minion of another culture; his text answers only a weapon
					// played on him
					put("chieftain", "1_266");
					// Goblin Armory: a [moria] condition in the support area, a [moria] card that is no minion; its
					// text answers only a [moria] weapon played, or a [moria] Orc's wound
					put("armory", "1_173");

					// Boromir, Son of Denethor: [gondor] Man companion, cost 3, vitality 3, no requirement to play
					put("boromir", "1_97");
					// Gimli, Son of Glóin: [dwarven] Dwarf companion, cost 2, vitality 3, no requirement to play
					put("gimli", "1_13");
					// Bounder: [shire] Hobbit ally, cost 1, vitality 2, no requirement to play
					put("bounder", "1_286");
					// Out of Sight and Shot: "Each time a companion is played, the Free Peoples player must exert a
					// companion"; cheated into play, so its own "spot a Nazgûl" is never asked
					put("outofsight", "7_204");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCompanionPlayedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("domain", "1_175");
					put("gimli", "1_13");       // Gimli, Son of Glóin: cost 2, vitality 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: cost 4, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowerPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("domain", "1_175");
					// Boromir, Son of Denethor: [gondor] Man companion, cost 3, vitality 3, no requirement to play
					put("boromir", "1_97");
					// Gimli, Son of Glóin: [dwarven] Dwarf companion, cost 2, vitality 3, no requirement to play
					put("gimli", "1_13");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinDomainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Domain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. To play, spot a [moria] minion. Plays to your support area.<br>Each time a companion is played to site 4 or higher, that companion comes into play exhausted.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("domain");

		assertEquals("Goblin Domain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionPlayedAtSite4OrHigherComesIntoPlayExhaustedAndOneAlreadyThereIsUntouched() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCompanionPlayedScenario();

		var domain = scn.GetShadowCard("domain");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(domain);
		scn.MoveCardsToHand(gimli, aragorn);

		scn.StartGame();
		scn.SkipToSite(3);

		// At site 3, one short of the site the text names, a companion comes into play as he always does.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, domain);
		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.SkipToSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsPlayAvailable(aragorn));

		scn.FreepsPlayCard(aragorn);
		// Vitality 4 with 3 wounds: exhausted.  Gimli, who was in play already, is as he was.
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsExhausted(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionPlayedAtSites4And5OfTheTowerPathComesIntoPlayAsHeAlwaysDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowerPathScenario();

		var domain = scn.GetShadowCard("domain");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(domain);
		scn.MoveCardsToHand(boromir, gimli);

		scn.StartGame();
		scn.SkipToSite(4);

		// The sites the text names by number are the Fellowship block's: the Tower path's sites 4 and 5 are not
		// among them, so the same numbers that exhaust a companion on the Fellowship path leave him whole here.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, domain);
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsPlayCard(boromir);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertFalse(scn.IsExhausted(boromir));

		scn.SkipToSite(5);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.IsExhausted(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAMoriaMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetShadowCard("domain");
		var runner = scn.GetShadowCard("runner");
		var chieftain = scn.GetShadowCard("chieftain");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToHand(domain, runner);
		scn.MoveCardsToSupportArea(armory);
		assertInZone(Zone.SUPPORT, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [sauron] Orc (a minion of another culture) and a [moria] condition (no minion) are all there is.
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertInZone(Zone.SUPPORT, armory);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(domain));

		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.ShadowPlayAvailable(domain));

		scn.ShadowPlayCard(domain);
		assertInZone(Zone.SUPPORT, domain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoriaMinionWhoIsNoOrcIsSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetShadowCard("domain");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToHand(domain);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(domain));

		scn.ShadowPlayCard(domain);
		assertInZone(Zone.SUPPORT, domain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCompanionPlayedAtSite5ComesIntoPlayExhaustedAndAnAllyDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetShadowCard("domain");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(boromir, gimli, bounder);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, domain);
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Vitality 3 each: exhausted is 2 wounds.
		scn.FreepsPlayCard(boromir);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.IsExhausted(boromir));

		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.IsExhausted(gimli));

		// An ally is no companion: he comes into play as he always does.
		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(0, scn.GetWoundsOn(bounder));

		// Frodo, a companion already in play, is not touched.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionIsExhaustedBeforeAnythingAnswersItsPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetShadowCard("domain");
		var outofsight = scn.GetShadowCard("outofsight");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(domain, outofsight);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(gimli);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.FreepsPlayCard(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));

		// Out of Sight and Shot answers Gimli's play: Gimli already came in exhausted, so he is not among those
		// who can exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
