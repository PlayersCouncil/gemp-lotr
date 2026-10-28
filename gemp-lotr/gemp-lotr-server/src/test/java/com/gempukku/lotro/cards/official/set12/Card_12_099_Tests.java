package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pitiless", "12_99");
					put("pitiless2", "12_99");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf; an archery ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, vitality 3; a skirmish ability
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit; his abilities answer nothing here
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PitilessOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Pitiless Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you play this minion, you may make the Free Peoples player exert a companion for each companion over 4.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("pitiless");

		assertEquals("Pitiless Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithFiveCompanionsTheFreePeoplesPlayerExertsOneCompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pitiless = scn.GetShadowCard("pitiless");
		var pitiless2 = scn.GetShadowCard("pitiless2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToHand(pitiless);
		// Five companions and an ally: a count of characters would be two over 4.
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, boromir);
		scn.MoveAlliesToTable(bounder);
		assertInHand(pitiless);

		scn.StartGame();
		// Gimli exhausted: vitality 3, 2 wounds
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(pitiless2);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.ShadowPlayCard(pitiless);

		// The copy already in play does not answer another minion's play.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pitiless));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pitiless2));
		scn.ShadowAcceptOptionalTrigger();

		// The Free Peoples player chooses among companions of four cultures who can exert, the Ring-bearer among them;
		// not the exhausted Gimli, the ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, legolas, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, bounder, pitiless, pitiless2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);

		// One over 4: one exertion, and nothing more is asked.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSixCompanionsTwoExertionsMayBothFallOnOneCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pitiless = scn.GetShadowCard("pitiless");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(pitiless);
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, boromir, sam);
		assertInHand(pitiless);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(pitiless);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pitiless));
		scn.ShadowAcceptOptionalTrigger();

		// Six companions, two over 4: two exertions, and "for each" lets one companion take both (CR 4.2 "for each").
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, legolas, gimli, boromir, sam));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFourCompanionsAndAnAllyThePlayOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pitiless = scn.GetShadowCard("pitiless");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToHand(pitiless);
		// Four companions and an ally: none over 4, though five characters.
		scn.MoveCompanionsToTable(aragorn, legolas, gimli);
		scn.MoveAlliesToTable(bounder);
		assertInHand(pitiless);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.ShadowPlayCard(pitiless);

		assertInZone(Zone.SHADOW_CHARACTERS, pitiless);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pitiless = scn.GetShadowCard("pitiless");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(pitiless);
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, boromir);
		assertInHand(pitiless);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.ShadowPlayCard(pitiless);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pitiless));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SHADOW_CHARACTERS, pitiless);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
