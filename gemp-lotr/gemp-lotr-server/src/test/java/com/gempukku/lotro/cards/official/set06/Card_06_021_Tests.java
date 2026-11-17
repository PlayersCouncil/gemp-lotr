package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_06_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("longbow1", "6_21");
					put("longbow2", "6_21");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, no archer; +3 only against a Nazgûl
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, no archer; +2 only at sites 6 to 8
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: Elf ally; a fellowship ability used only on purpose
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: [gondor] Man companion, Defender +1 and no other text
					put("bow", "1_41");         // Elven Bow: an [elven] possession, to be discarded from the deck
					put("lorienelf", "1_53");   // Lórien Elf: an [elven] companion card, to be discarded from the deck
					put("lightfoot", "1_52");   // Lightfootedness: an [elven] event, to be discarded from the deck
					put("armor", "1_92");       // Armor: a [gondor] possession, to be discarded from the deck

					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1: cannot exert
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NaithLongbowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Naith Longbow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Strength: 1
		 * Game Text: Bearer must be an Elf companion.<br><b>Archery:</b> Exert bearer to discard the top card of your draw deck. If it is an [elven] card, exert a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("longbow1");

		assertEquals("Naith Longbow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnElfCompanionOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longbow = scn.GetFreepsCard("longbow1");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, haldir, aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(longbow);

		scn.StartGame();

		assertInZone(Zone.HAND, longbow);
		assertTrue(scn.FreepsPlayAvailable(longbow));
		scn.FreepsPlayCard(longbow);
		// Two Elf companions; not an Elf ally, nor companions who are no Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, haldir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(haldir);

		assertAttachedTo(longbow, haldir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryActionExertsItsBearerAndEachElvenCardDiscardedExertsAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longbow1 = scn.GetFreepsCard("longbow1");
		var longbow2 = scn.GetFreepsCard("longbow2");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var bow = scn.GetFreepsCard("bow");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var lightfoot = scn.GetFreepsCard("lightfoot");
		var party = scn.GetShadowCard("party");
		var patrol = scn.GetShadowCard("patrol");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.AttachCardsTo(arwen, longbow1);
		scn.AttachCardsTo(haldir, longbow2);

		scn.StartGame();
		scn.MoveMinionsToTable(party, patrol, runner);
		// The last named is the top card.
		scn.MoveCardsToTopOfDeck(lightfoot, lorienelf, bow);
		assertInZone(Zone.DECK, lightfoot, lorienelf, bow);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertSame(bow, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertTrue(scn.FreepsActionAvailable(longbow1));

		// An [elven] possession.
		scn.FreepsUseCardAction(longbow1);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertInDiscard(bow);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// Minions of two cultures and races; not one that cannot exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(party, patrol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(party);
		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		// An [elven] companion card, by the other copy's bearer.
		scn.ShadowPass();
		scn.FreepsUseCardAction(longbow2);
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInDiscard(lorienelf);
		scn.FreepsChooseCard(patrol);
		assertEquals(1, scn.GetWoundsOn(patrol));

		// An [elven] event.
		scn.ShadowPass();
		scn.FreepsUseCardAction(longbow1);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertInDiscard(lightfoot);
		scn.FreepsChooseCard(party);
		assertEquals(2, scn.GetWoundsOn(party));
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ANonElvenCardDiscardedExertsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longbow = scn.GetFreepsCard("longbow1");
		var arwen = scn.GetFreepsCard("arwen");
		var armor = scn.GetFreepsCard("armor");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, longbow);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.MoveCardsToTopOfDeck(armor);
		assertInZone(Zone.DECK, armor);

		scn.SkipToPhase(Phase.ARCHERY);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		var shadowTop = scn.GetShadowTopOfDeck();
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(party));

		scn.FreepsUseCardAction(longbow);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInDiscard(armor);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// "Your draw deck": the Shadow player's is untouched.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertSame(shadowTop, scn.GetShadowTopOfDeck());
		// A [gondor] card: the one minion, which an [elven] card would exert unasked, is not exerted.
		assertEquals(0, scn.GetWoundsOn(party));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryActionIsNotOfferedInTheManeuverPhaseNorWithItsBearerExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longbow1 = scn.GetFreepsCard("longbow1");
		var longbow2 = scn.GetFreepsCard("longbow2");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.AttachCardsTo(arwen, longbow1);
		scn.AttachCardsTo(haldir, longbow2);
		scn.AddWoundsToChar(arwen, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		scn.MoveMinionsToTable(party);

		// The phase before: Haldir could exert, and his copy is not offered.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(longbow2));

		// Vitality 3 with 2 wounds: Arwen is exhausted, so her copy is not offered; Haldir's is.
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(longbow1));
		assertTrue(scn.FreepsActionAvailable(longbow2));
	}

	@Test
	public void WithTheDrawDeckEmptyTheArcheryActionIsStillOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longbow = scn.GetFreepsCard("longbow1");
		var arwen = scn.GetFreepsCard("arwen");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, longbow);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SetFreepsDrawDeck();

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetFreepsDeckCount());
		int discard = scn.GetFreepsDiscardCount();
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(party));
		assertTrue(scn.FreepsActionAvailable(longbow));

		scn.FreepsUseCardAction(longbow);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetWoundsOn(party));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
