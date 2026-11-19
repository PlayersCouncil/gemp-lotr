package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Dwarven Axe: [dwarven] hand weapon possession, cost 1, bearer a [dwarven] character.
					put("axe", "31_1");
					// Iron-Forged Weapon: [esgaroth] hand weapon possession, cost 2, bearer a Man or a [dwarven]
					// companion.  A skirmish ability.
					put("iron", "32_18");
					// Sting: [shire] hand weapon ARTIFACT, cost 1, bearer Bilbo.  A skirmish-win trigger.
					put("sting", "30_47");
					// Black Arrow: [esgaroth] RANGED weapon possession, cost 1, bearer Bard.  An archery ability.
					put("arrow", "32_16");
					// Mithril Coat: [dwarven] armor artifact, cost 2, bearer Bilbo.  The minion archery total is -1.
					put("coat", "31_5");
					// Thorin, Oakenshield: [dwarven] Dwarf companion, to bear the hand weapons.
					put("thorin", "30_19");
					// Bard, The Bowman: [esgaroth] Man ally, to bear the Black Arrow.  A regroup ability.
					put("bard", "30_22");
					// Goblin Patrol Troop: a card with no text, laid on top of a draw deck so that the hands drawn
					// before site 2 do not take the cards the offer is to find.
					for (int i = 1; i <= 8; i++)
						put("troop" + i, "1_177");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheTrollHoardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: The Troll Hoard
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 2
		 * Shadow Number: 3
		 * Direction: none
		 * Game Text: <b>Forest</b>. <b>Underground</b>. At the start of your fellowship phase, you may play a hand weapon from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("The Troll Hoard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseAHandWeaponMayBePlayedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var iron = scn.GetFreepsCard("iron");
		var sting = scn.GetFreepsCard("sting");
		var arrow = scn.GetFreepsCard("arrow");
		var coat = scn.GetFreepsCard("coat");
		var thorin = scn.GetFreepsCard("thorin");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		assertInZone(Zone.SUPPORT, bard);
		// The deck is laid before the move: the offer is worked out as the fellowship phase begins.
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"), scn.GetFreepsCard("troop6"), scn.GetFreepsCard("troop7"), scn.GetFreepsCard("troop8"),
				axe, iron, sting, arrow, coat);
		// Player 1's second turn begins at The Troll Hoard, its offer pending.
		scn.SkipToSite(2);

		assertEquals("30_50", scn.GetCurrentSite().getBlueprintId());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertInZone(Zone.DECK, axe, iron, sting, arrow, coat);
		assertEquals(5, scn.GetFreepsDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		// Hand weapons of three cultures, possessions and an artifact; not a ranged weapon, nor an armor.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, iron, sting));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arrow, coat));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(axe);

		// Thorin is the only [dwarven] character, so the Axe goes on him unasked.
		assertAttachedTo(axe, thorin);
		assertInZone(Zone.DECK, iron, sting, arrow, coat);
		assertEquals(4, scn.GetFreepsDeckCount());
		// One shuffle for the card leaving the deck that was looked through, one for the look itself.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningPlaysNothingAndTheDrawDeckIsNotLookedThrough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var sting = scn.GetFreepsCard("sting");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"), scn.GetFreepsCard("troop6"), scn.GetFreepsCard("troop7"), scn.GetFreepsCard("troop8"),
				axe, sting);
		scn.SkipToSite(2);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertInZone(Zone.DECK, axe, sting);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int twilight = scn.GetTwilight();

		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DECK, axe, sting);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.GetAttachedCards(thorin).isEmpty());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoHandWeaponInTheDrawDeckTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var coat = scn.GetFreepsCard("coat");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bard);
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"), scn.GetFreepsCard("troop6"), scn.GetFreepsCard("troop7"), scn.GetFreepsCard("troop8"),
				arrow, coat);
		scn.SkipToSite(2);

		assertEquals("30_50", scn.GetCurrentSite().getBlueprintId());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertInZone(Zone.DECK, arrow, coat);
		// A ranged weapon Bard could bear and an armor: nothing the site could play.  A Hobbit-set play from the
		// draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418; F-0800).
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedToThePlayerWhoseFellowshipPhaseBeginsThereAndNotOnceTheFellowshipHasMovedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowSting = scn.GetShadowCard("sting");
		var shadowCoat = scn.GetShadowCard("coat");
		var shadowBilbo = scn.GetShadowRingBearer();

		scn.StartGame();
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"), scn.GetFreepsCard("troop6"), scn.GetFreepsCard("troop7"), scn.GetFreepsCard("troop8"),
				scn.GetFreepsCard("sting"));
		scn.SetShadowDrawDeck(scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"), scn.GetShadowCard("troop3"), scn.GetShadowCard("troop4"), scn.GetShadowCard("troop5"), scn.GetShadowCard("troop6"), scn.GetShadowCard("troop7"), scn.GetShadowCard("troop8"),
				shadowSting, shadowCoat);
		scn.SkipToSite(2);

		// Player 1's turn at The Troll Hoard: he is offered, and declines.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Player 2's turn begins there too: it is his fellowship phase, and his offer.
		scn.SkipToOtherPlayersTurn();

		assertEquals("30_50", scn.GetCurrentSite().getBlueprintId());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertInZone(Zone.DECK, shadowSting, shadowCoat);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.ShadowAcceptOptionalTrigger();
		scn.DismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(shadowSting));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shadowCoat));
		scn.ShadowChooseCardBPFromSelection(shadowSting);

		assertAttachedTo(shadowSting, shadowBilbo);
		assertInZone(Zone.DECK, shadowCoat);

		// Player 1's next turn begins at Rivendell, with The Troll Hoard behind: no offer.
		scn.SkipToOtherPlayersTurn();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
