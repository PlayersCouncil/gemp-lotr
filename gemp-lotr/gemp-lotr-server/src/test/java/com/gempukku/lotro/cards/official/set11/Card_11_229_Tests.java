package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_229_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gimli, Son of Glóin: a Dwarf companion, vitality 3.  His skirmish ability is not used.
					put("gimli", "1_13");
					// Grimir, Dwarven Elder: a Dwarf ally, vitality 3.  His fellowship ability is not used.
					put("grimir", "1_17");
					// Legolas, Greenleaf: an Elf companion, the Elf that Elven Brooch spots.  Archery is never reached.
					put("legolas", "1_50");
					// Dwarven Armor: a [dwarven] possession, cost 0, borne by a Dwarf; no ability
					put("armor", "1_8");
					// Horn of Helm: a [dwarven] possession, unique, cost 2, to the support area
					put("horn", "5_8");
					// Ring of Guile: a [dwarven] ARTIFACT, cost 0, borne by a Dwarf
					put("guile", "9_8");
					// Dwarven Heart: a [dwarven] CONDITION, cost 0, borne by a Dwarf
					put("heart", "1_10");
					// Elven Brooch: an [elven] possession, cost 1.  To play, spot an Elf.  Bearer must be a companion.
					put("brooch", "4_63");
				}},
				new HashMap<>() {{
					put("barazinbar", "11_229");
					put("site2", "13_185");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void BarazinbarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Barazinbar
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Right
		 * Game Text: <b>Mountain</b>. At the start of your fellowship phase, you may exert a Dwarf to play a [dwarven] possession from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("barazinbar");

		assertEquals("Barazinbar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void ADwarfExertsToPlayADwarvenPossessionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gimli, legolas);
		scn.MoveAlliesToTable(grimir);

		var armor = scn.GetFreepsCard("armor");
		var horn = scn.GetFreepsCard("horn");
		var guile = scn.GetFreepsCard("guile");
		var heart = scn.GetFreepsCard("heart");
		var brooch = scn.GetFreepsCard("brooch");

		var barazinbar = scn.GetFreepsCard("barazinbar");

		scn.StartGame(barazinbar);

		assertSame(barazinbar, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DECK, armor, horn, guile, heart, brooch);

		// Enough in the pool for any card of the deck: the Horn costs 2.
		scn.SetTwilight(5);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();

		// A Dwarf companion and a Dwarf ally may exert.  An Elf and the Ring-bearer, a Hobbit, may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.FreepsChooseCard(grimir);

		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		scn.FreepsDismissRevealedCards();

		// A [dwarven] possession borne by a Dwarf and one that goes to the support area may be played.  Not a
		// [dwarven] artifact, a [dwarven] condition, or a possession of another culture that a Dwarf could bear.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, horn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guile, heart, brooch));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(horn);

		assertInZone(Zone.SUPPORT, horn);
		assertInZone(Zone.DECK, armor, guile, heart, brooch);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// The deck is shuffled as the card leaves it, and again when the look is over.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheOnlyDwarfExhaustedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gimli, legolas);
		// Gimli is exhausted.  Frodo and Legolas could each exert, and neither is a Dwarf.
		scn.AddWoundsToChar(gimli, 2);

		var armor = scn.GetFreepsCard("armor");
		var barazinbar = scn.GetFreepsCard("barazinbar");

		scn.StartGame(barazinbar);

		assertSame(barazinbar, scn.GetCurrentSite());
		assertInZone(Zone.DECK, armor);
		assertTrue(scn.IsExhausted(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningExertsNobodyAndTheOtherPlayerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToTopOfDeck(armor);

		// The second player has a Dwarf of his own on the table, able to exert.
		var shadowGimli = scn.GetShadowCard("gimli");
		scn.MoveCompanionsToTable(shadowGimli);

		var barazinbar = scn.GetFreepsCard("barazinbar");

		scn.StartGame(barazinbar);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsDeclineOptionalTrigger();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(shadowGimli));
		assertInZone(Zone.DECK, armor);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheDwarfExertsAndThePlayerMayStillPlayNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);

		var barazinbar = scn.GetFreepsCard("barazinbar");

		scn.StartGame(barazinbar);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// Gimli is the only Dwarf: he exerts without a choice.
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor));
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, armor);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenPossessionInTheDrawDeckTheOfferIsStillMadeAndTheExertionBuysALookThatFindsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gimli, legolas);
		// Both [dwarven] possessions are taken out of the deck.  The Ring of Guile, Dwarven Heart, Elven Brooch
		// and Grimir are left in it.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("armor"), scn.GetFreepsCard("horn"));

		var barazinbar = scn.GetFreepsCard("barazinbar");

		scn.StartGame(barazinbar);

		assertEquals(4, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.SetTwilight(5);

		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		// The deck is shown, and nothing in it may be chosen.
		scn.FreepsDismissRevealedCards();

		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedToThePlayerWhoseFellowshipIsAtBarazinbarAndNotOnceItHasMovedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);

		var shadowGimli = scn.GetShadowCard("gimli");
		var shadowArmor = scn.GetShadowCard("armor");
		scn.MoveCompanionsToTable(shadowGimli);

		var barazinbar = scn.GetFreepsCard("barazinbar");
		var ruins = scn.GetShadowSite("site5");     // Sirannon Ruins: a river, and no other text

		scn.StartGame(barazinbar);

		// The first player's turn: he is offered and declines.  The second player, whose own Gimli could exert,
		// is offered nothing on a turn that is not his.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(shadowGimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// He moves on to site 2.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertEquals(2, scn.FreepsGetSiteNumber());

		// The second player's turn begins at site 1, which is the first player's card: the offer is the
		// second player's.  His own Gimli exerts, and the Armor from his own draw deck goes on Gimli, his
		// only Dwarf.
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertSame(barazinbar, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.MoveCardsToTopOfDeck(shadowArmor);
		assertEquals(0, scn.GetWoundsOn(shadowGimli));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(shadowGimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(shadowArmor);
		assertAttachedTo(shadowArmor, shadowGimli);

		// The first player's second turn begins at site 2, with Gimli and Grimir able to exert.  Barazinbar lies
		// behind him, and nothing is offered.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertSame(barazinbar, scn.GetSite(1));
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
