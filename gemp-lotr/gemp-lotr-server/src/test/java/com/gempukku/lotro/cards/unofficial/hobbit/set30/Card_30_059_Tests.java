package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_059_Tests
{

	// The Hobbit table, on the Hobbit path with the Free Peoples Pack's site 2 in place of The Troll Hoard:
	// Trollshaw Forest is site 2, the fellowship's first move from Bag End 30_49.  Bilbo, Reliable Companion 30_46
	// is the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Free Peoples hand weapons, and two items that are none.
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

					// Shadow hand weapons, and two items that are none.
					// Troll Knife: [troll] hand weapon possession, cost 2, bearer a Troll; bearer is damage +1.
					put("knife", "31_60");
					// Goblin Scimitar: [moria] hand weapon possession, cost 0, bearer a [moria] Orc; may draw a card
					// when played (not played here).
					put("scimitar", "1_180");
					// Goblin Bow: [moria] RANGED weapon possession, cost 0, bearer a [moria] Orc.
					put("bow", "33_40");
					// Boulder Rock: [moria] possession with no item class, cost 1, bearer a Troll or a Giant.
					put("rock", "33_35");
					// Goblin Patrol Troop: [moria] Orc minion, no text.
					put("troop", "1_177");
					// Demolition Troll: [moria] Troll minion; a Skirmish ability, not used.
					put("troll", "32_31");
				}},
				new HashMap<>()
				{{
					put("site1", "30_49");
					put("site2", "30_59");
					put("site3", "30_51");
					put("site4", "30_52");
					put("site5", "30_53");
					put("site6", "30_54");
					put("site7", "30_55");
					put("site8", "30_56");
					put("site9", "30_57");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TrollshawForestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Trollshaw Forest
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 2
		 * Shadow Number: 2
		 * Direction: none
		 * Game Text: <b>Forest</b>. At the start of the maneuver phase, each player may play a hand weapon from his or her draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Trollshaw Forest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseEachPlayerMayPlayAHandWeaponFromHisOwnDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var iron = scn.GetFreepsCard("iron");
		var sting = scn.GetFreepsCard("sting");
		var arrow = scn.GetFreepsCard("arrow");
		var coat = scn.GetFreepsCard("coat");
		var thorin = scn.GetFreepsCard("thorin");
		var bard = scn.GetFreepsCard("bard");
		var knife = scn.GetShadowCard("knife");
		var scimitar = scn.GetShadowCard("scimitar");
		var bow = scn.GetShadowCard("bow");
		var rock = scn.GetShadowCard("rock");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		scn.SetFreepsDrawDeck(axe, iron, sting, arrow, coat);
		scn.SetShadowDrawDeck(knife, scimitar, bow, rock);
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		assertInZone(Zone.SUPPORT, bard);
		assertInZone(Zone.DECK, axe, iron, sting, arrow, coat, knife, scimitar, bow, rock);
		scn.FreepsPass();

		assertEquals("30_59", scn.GetCurrentSite().getBlueprintId());
		scn.MoveMinionsToTable(troop, troll);
		scn.SetTwilight(10);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, troll);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowPass();

		// The Free Peoples player is asked first.
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertTrue(scn.FreepsDecisionAvailable("hand weapon"));
		scn.FreepsChooseYes();
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
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());

		// Then the Shadow player, from his own deck.
		assertTrue(scn.ShadowDecisionAvailable("hand weapon"));
		scn.ShadowChooseYes();
		scn.ShadowDismissRevealedCards();

		// Shadow hand weapons of two cultures; not a ranged weapon, nor a possession with no class.
		assertTrue(scn.ShadowHasCardChoiceAvailable(knife, scimitar));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bow, rock));
		scn.ShadowChooseCardBPFromSelection(knife);

		// The Troll is the only Troll, so the Knife goes on him unasked.
		assertAttachedTo(knife, troll);
		assertInZone(Zone.DECK, scimitar, bow, rock);
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EachPlayerMayDeclineAndNothingIsPlayedNorIsItAskedAgainAtTheStartOfTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var thorin = scn.GetFreepsCard("thorin");
		var knife = scn.GetShadowCard("knife");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.SetFreepsDrawDeck(axe);
		scn.SetShadowDrawDeck(knife);
		assertInZone(Zone.DECK, axe, knife);
		scn.FreepsPass();

		scn.MoveMinionsToTable(troll);
		scn.SetTwilight(10);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowPass();

		assertTrue(scn.FreepsDecisionAvailable("hand weapon"));
		scn.FreepsChooseNo();
		// His decline leaves the other player's question standing.
		assertTrue(scn.ShadowDecisionAvailable("hand weapon"));
		scn.ShadowChooseNo();

		assertInZone(Zone.DECK, axe, knife);
		assertTrue(scn.GetAttachedCards(thorin).isEmpty());
		assertTrue(scn.GetAttachedCards(troll).isEmpty());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The next phase begins with no question.
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertFalse(scn.FreepsDecisionAvailable("hand weapon"));
		assertFalse(scn.ShadowDecisionAvailable("hand weapon"));
		assertTrue(scn.FreepsDecisionAvailable("Play Archery action or Pass"));
	}

	@Test
	public void WithNoHandWeaponInHisDrawDeckAPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var coat = scn.GetFreepsCard("coat");
		var bard = scn.GetFreepsCard("bard");
		var troll = scn.GetShadowCard("troll");
		var knife = scn.GetShadowCard("knife");
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		scn.SetFreepsDrawDeck(arrow, coat);
		assertInZone(Zone.SUPPORT, bard);
		assertInZone(Zone.DECK, arrow, coat);
		scn.FreepsPass();

		// The Shadow player's deck holds a hand weapon his Troll could bear.
		scn.MoveMinionsToTable(troll);
		scn.SetShadowDrawDeck(knife);
		scn.SetTwilight(10);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.ShadowPass();

		// A ranged weapon and an armor: nothing the site could play from the first player's deck.  A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418):
		// he is not asked, and his deck is not looked through.
		assertFalse(scn.FreepsDecisionAvailable("hand weapon"));
		assertInZone(Zone.DECK, arrow, coat);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		assertTrue(scn.ShadowDecisionAvailable("hand weapon"));
		scn.ShadowChooseNo();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AskedAgainAtTheNextManeuverPhaseThereWithTheOtherPlayersFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var shadowSting = scn.GetShadowCard("sting");
		var shadowCoat = scn.GetShadowCard("coat");
		var shadowBilbo = scn.GetShadowRingBearer();
		var freepsKnife = scn.GetFreepsCard("knife");
		var freepsBow = scn.GetFreepsCard("bow");
		var freepsTroll = scn.GetFreepsCard("troll");

		scn.StartGame();
		scn.FreepsPass();

		// Player 1's maneuver phase at Trollshaw Forest: each player is asked, and each declines.
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("hand weapon"));
		scn.FreepsChooseNo();
		assertTrue(scn.ShadowDecisionAvailable("hand weapon"));
		scn.ShadowChooseNo();
		// The minion is put away, so that no skirmish is fought on the way to the next turn.
		scn.MoveCardsToDiscard(troop);
		assertInZone(Zone.DISCARD, troop);

		// Player 2's fellowship moves to the same site on his own turn; player 1 is now the Shadow player.
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();
		assertEquals("30_59", scn.GetCurrentSite().getBlueprintId());
		scn.SetShadowDrawDeck(shadowSting, shadowCoat);
		scn.SetFreepsDrawDeck(freepsKnife, freepsBow);
		scn.MoveMinionsToTable(freepsTroll);
		scn.SetTwilight(10);
		assertInZone(Zone.DECK, shadowSting, shadowCoat, freepsKnife, freepsBow);
		assertInZone(Zone.SHADOW_CHARACTERS, freepsTroll);
		scn.FreepsPass();

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		// Player 2, whose fellowship it is, first, from his own deck.
		assertTrue(scn.ShadowDecisionAvailable("hand weapon"));
		scn.ShadowChooseYes();
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(shadowSting));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shadowCoat));
		scn.ShadowChooseCardBPFromSelection(shadowSting);
		assertAttachedTo(shadowSting, shadowBilbo);

		// Then player 1, from his.
		assertTrue(scn.FreepsDecisionAvailable("hand weapon"));
		scn.FreepsChooseYes();
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(freepsKnife));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(freepsBow));
		scn.FreepsChooseCardBPFromSelection(freepsKnife);
		assertAttachedTo(freepsKnife, freepsTroll);

		assertInZone(Zone.DECK, shadowCoat, freepsBow);
		assertTrue(scn.ShadowDecisionAvailable("Play Maneuver action or Pass"));
	}
}
