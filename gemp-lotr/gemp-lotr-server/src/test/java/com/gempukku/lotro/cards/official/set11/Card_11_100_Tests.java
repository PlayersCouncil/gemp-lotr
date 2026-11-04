package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("men1", "11_100");
					put("men2", "11_100");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion; its trigger is on its play only, and it is cheated in

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: Dwarf, no text but a requirement to play
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, whose only text is a maneuver ability used on purpose

					put("brand", "2_32");       // Flaming Brand: Free Peoples possession, cost 0. Bearer must be a Man
					put("words", "19_9");       // Stern Words: Free Peoples condition, cost 2. Bearer must be a companion
					put("lathspell", "11_87");  // Lathspell: Shadow condition, cost 3. Bearer must be a companion
					put("poleaxe", "11_95");    // Poleaxe: Shadow possession, cost 1. Bearer must be a [men] minion
					put("fury", "9_7");         // Ring of Fury: Free Peoples artifact. Bearer must be a Dwarf
					put("legacy", "1_282");     // The Weight of a Legacy: Shadow condition. Plays on Aragorn
					put("fear", "1_278");       // Strength Born of Fear: Shadow condition in the support area
					put("snows", "1_138");      // Saruman's Snows: Shadow condition. Plays on a site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrangelookingMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Strange-looking Men
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Maneuver:</b> Exert this minion to transfer a possession or condition borne by a character to another eligible bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("men1");

		assertEquals("Strange-looking Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsThisMinionToTransferAPossessionOrConditionBorneByACharacterToAnotherEligibleBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var brand = scn.GetFreepsCard("brand");
		var words = scn.GetFreepsCard("words");
		var fury = scn.GetFreepsCard("fury");

		var men1 = scn.GetShadowCard("men1");
		var men2 = scn.GetShadowCard("men2");
		var brigand = scn.GetShadowCard("brigand");
		var lathspell = scn.GetShadowCard("lathspell");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var legacy = scn.GetShadowCard("legacy");
		var fear = scn.GetShadowCard("fear");
		var snows = scn.GetShadowCard("snows");

		scn.MoveCompanionsToTable(aragorn, boromir, gimli, guard);
		// Possessions and conditions that characters bear, of both sides and on both sides.
		scn.AttachCardsTo(boromir, brand);
		scn.AttachCardsTo(gimli, words);
		scn.AttachCardsTo(aragorn, lathspell);
		// An artifact that the other Dwarf could bear, and a condition that only Aragorn can bear.
		scn.AttachCardsTo(gimli, fury);
		scn.AttachCardsTo(aragorn, legacy);
		// A condition that nothing bears.
		scn.MoveCardsToSupportArea(fear);
		assertAttachedTo(brand, boromir);
		assertAttachedTo(words, gimli);
		assertAttachedTo(lathspell, aragorn);
		assertAttachedTo(fury, gimli);
		assertAttachedTo(legacy, aragorn);
		assertInZone(Zone.SUPPORT, fear);

		scn.StartGame();
		scn.MoveMinionsToTable(men1, men2, brigand);
		scn.AttachCardsTo(men2, poleaxe);
		// A condition borne by a site, with a second site on the table that could bear it.
		scn.AttachCardsTo(scn.GetSite(1), snows);
		assertAttachedTo(poleaxe, men2);
		assertAttachedTo(snows, scn.GetSite(1));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 5 companions + 2 for the site.
		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(men1));
		assertEquals(0, scn.GetWoundsOn(men2));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertTrue(scn.ShadowActionAvailable(men1));
		assertTrue(scn.ShadowActionAvailable(men2));

		scn.ShadowUseCardAction(men1);
		// The copy used exerts, and not the other copy or the other minion.
		assertEquals(1, scn.GetWoundsOn(men1));
		assertEquals(0, scn.GetWoundsOn(men2));
		assertEquals(0, scn.GetWoundsOn(brigand));

		assertTrue(scn.ShadowHasCardChoiceAvailable(brand, words, lathspell, poleaxe));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(fury, legacy, fear, snows));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lathspell);

		// Lathspell's bearer must be a companion: any other companion, the Ring-bearer included, and no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, boromir, gimli, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, men1, men2, brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertAttachedTo(lathspell, gimli);
		assertNotAttachedTo(lathspell, aragorn);
		// Lathspell costs 3, and nothing is paid for a transfer to another eligible bearer.
		assertEquals(7, scn.GetTwilight());
		assertAttachedTo(brand, boromir);
		assertAttachedTo(words, gimli);
		assertAttachedTo(poleaxe, men2);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The other copy moves the Poleaxe off itself.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(men2));
		scn.ShadowUseCardAction(men2);
		assertEquals(1, scn.GetWoundsOn(men1));
		assertEquals(1, scn.GetWoundsOn(men2));

		assertTrue(scn.ShadowHasCardChoiceAvailable(brand, words, lathspell, poleaxe));
		scn.ShadowChooseCard(poleaxe);

		// Poleaxe's bearer must be a [men] minion: the other copy is the only one, the Brigand being a [dunland] Man.
		assertAttachedTo(poleaxe, men1);
		assertNotAttachedTo(poleaxe, men2);
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void FlamingBrandMayBeTransferredToAnyOtherManFreePeoplesOrShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var brand = scn.GetFreepsCard("brand");

		var men1 = scn.GetShadowCard("men1");
		var brigand = scn.GetShadowCard("brigand");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.AttachCardsTo(boromir, brand);
		assertAttachedTo(brand, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(men1, brigand, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(men1));
		scn.ShadowUseCardAction(men1);
		assertEquals(1, scn.GetWoundsOn(men1));

		// Flaming Brand is the only card to transfer.  Its bearer must be a Man: the other Man of the fellowship
		// and both Men among the minions, and not its bearer, the Dwarf, the Hobbit or the Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, men1, brigand));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, gimli, frodo, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(brigand);

		assertAttachedTo(brand, brigand);
		assertNotAttachedTo(brand, boromir);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnAllyMayBearTheTransferredCardAndGiveItUp() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		var brand = scn.GetFreepsCard("brand");

		var men1 = scn.GetShadowCard("men1");
		var men2 = scn.GetShadowCard("men2");

		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(boromir, brand);
		assertAttachedTo(brand, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(men1, men2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(men1));
		scn.ShadowUseCardAction(men1);
		assertEquals(1, scn.GetWoundsOn(men1));

		// Flaming Brand is the only card to transfer.  An ally is a character and a Man: the Merchant is offered
		// beside the companion and the minions that are Men, and not its bearer, the Dwarf or the Hobbit.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, merchant, men1, men2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, gimli, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(merchant);
		assertAttachedTo(brand, merchant);
		assertNotAttachedTo(brand, boromir);

		// The card is now borne by an ally, and the second copy can move it on: it is a card borne by a character,
		// and the ally, its bearer, is not offered again.
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(men2));
		scn.ShadowUseCardAction(men2);
		assertEquals(1, scn.GetWoundsOn(men2));

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, boromir, men1, men2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(merchant, gimli, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);
		assertAttachedTo(brand, aragorn);
		assertNotAttachedTo(brand, merchant);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnExhaustedCopyIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var brand = scn.GetFreepsCard("brand");

		var men1 = scn.GetShadowCard("men1");
		var men2 = scn.GetShadowCard("men2");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(boromir, brand);
		assertAttachedTo(brand, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(men1, men2);
		// Vitality 2 with 1 wound: this copy cannot exert.
		scn.AddWoundsToChar(men1, 1);
		assertEquals(1, scn.GetWoundsOn(men1));
		assertEquals(0, scn.GetWoundsOn(men2));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(men1));
		assertTrue(scn.ShadowActionAvailable(men2));

		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(men1));
		assertEquals(0, scn.GetWoundsOn(men2));
		assertAttachedTo(brand, boromir);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithNothingToTransferItExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var fury = scn.GetFreepsCard("fury");

		var men1 = scn.GetShadowCard("men1");
		var fear = scn.GetShadowCard("fear");

		scn.MoveCompanionsToTable(gimli, guard);
		// An artifact on a character and a condition in the support area: nothing the minion can move.
		scn.AttachCardsTo(gimli, fury);
		scn.MoveCardsToSupportArea(fear);
		assertAttachedTo(fury, gimli);
		assertInZone(Zone.SUPPORT, fear);

		scn.StartGame();
		scn.MoveMinionsToTable(men1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(men1));
		assertTrue(scn.ShadowActionAvailable(men1));

		scn.ShadowUseCardAction(men1);

		assertEquals(1, scn.GetWoundsOn(men1));
		assertAttachedTo(fury, gimli);
		assertInZone(Zone.SUPPORT, fear);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
