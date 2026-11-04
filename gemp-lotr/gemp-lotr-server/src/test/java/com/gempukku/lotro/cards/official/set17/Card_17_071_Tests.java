package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grishnakh", "17_71");

					// Denizen of Khazad-dum: an [orc] Orc, no hunter; +2 at an underground site only
					put("denizen", "11_115");
					// Orkish Cavalry: an [orc] Orc, no hunter; a draw when an [orc] mount is played, and an
					// Assignment ability that is not used
					put("cavalry", "17_74");
					// Half-troll of Far Harad: an [orc] minion who is no Orc; its start-of-assignment trigger is
					// not reached
					put("halftroll", "14_12");
					// Mordor Brute: a [sauron] Orc; a [sauron] Orc bearing a possession takes no wounds outside
					// skirmishes
					put("brute", "10_90");

					// Tracking Uruk: an [uruk-hai] Uruk-hai, cost 5, Hunter 1 its whole text
					put("tracking", "15_171");
					// White Hand Butcher: an [uruk-hai] Uruk-hai, cost 4, Hunter 6 its whole text
					put("butcher", "17_122");
					// Uruk Decimator: an [uruk-hai] Uruk-hai with no hunter; +2 at a battleground, damage +1 while
					// bearing a possession
					put("decimator", "12_150");
					// Orkish Breeder: an [orc] Orc, Hunter 1; a Shadow ability that is not used
					put("breeder", "18_87");
					// Saruman, Master of the White Hand: an [uruk-hai] Wizard, a minion who is no Uruk-hai; each
					// companion is resistance -1, and a Skirmish ability that is not used
					put("saruman", "17_116");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, Damage +1 its whole text
					put("guard", "10_81");
					// Dark Alliance: an [uruk-hai] condition, no minion; a Response that is not used
					put("alliance", "12_141");
					// Ugluk, Ugly Fellow: the [uruk-hai] Ugluk, Hunter 3; at the start of his skirmishes non-hunters
					// skirmishing him exert (no skirmish is fought here)
					put("ugluk", "15_172");
					// Ugluk, Servant of Saruman: the [isengard] Ugluk; his texts read [isengard] trackers only
					put("isengardugluk", "4_176");

					// Aragorn, Ranger of the North: a companion, no hunter; his Maneuver ability is not used
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrishnakhStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Grishnákh, Treacherous Captain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Hunter 2</b>.<br>While you can spot an [uruk-hai] minion, each [orc] Orc gains <b>hunter 1</b> (if that [uruk-hai] minion is Uglúk, each [orc] Orc gains an additional <b>hunter 1</b>).<br><b>Shadow:</b> Exert Grishnákh twice to play an [uruk-hai] hunter minion from your draw deck.
		*/

		var scn = GetScenario();

		var grishnakh = scn.GetFreepsCard("grishnakh");

		assertEquals("Grishnákh", grishnakh.getBlueprint().getTitle());
		assertEquals("Treacherous Captain", grishnakh.getBlueprint().getSubtitle());
		assertTrue(grishnakh.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, grishnakh.getBlueprint().getSide());
		assertEquals(Culture.ORC, grishnakh.getBlueprint().getCulture());
		assertEquals(CardType.MINION, grishnakh.getBlueprint().getCardType());
		assertEquals(Race.ORC, grishnakh.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(grishnakh, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(grishnakh, Keyword.HUNTER));
		assertEquals(4, grishnakh.getBlueprint().getTwilightCost());
		assertEquals(11, grishnakh.getBlueprint().getStrength());
		assertEquals(3, grishnakh.getBlueprint().getVitality());
		assertEquals(4, grishnakh.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileAnUrukHaiMinionCanBeSpottedEachOrcOrcGainsHunter1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var grishnakh = scn.GetShadowCard("grishnakh");
		var denizen = scn.GetShadowCard("denizen");
		var cavalry = scn.GetShadowCard("cavalry");
		var halftroll = scn.GetShadowCard("halftroll");
		var brute = scn.GetShadowCard("brute");
		var guard = scn.GetShadowCard("guard");
		var alliance = scn.GetShadowCard("alliance");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCardsToHand(tracking);
		assertInHand(tracking);

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, denizen, cavalry, halftroll, brute, guard);
		scn.MoveCardsToSupportArea(alliance);
		assertInZone(Zone.SUPPORT, alliance);
		scn.SetTwilight(10);

		// A [sauron] Uruk-hai and an [uruk-hai] condition, but no [uruk-hai] minion: nothing is gained.
		assertEquals(2, scn.GetKeywordCount(grishnakh, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(denizen, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(cavalry, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(halftroll, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(brute, Keyword.HUNTER));

		scn.FreepsPass();
		scn.ShadowPlayCard(tracking);
		assertInZone(Zone.SHADOW_CHARACTERS, tracking);

		// Each [orc] Orc, Grishnakh among them (2 + 1); not the [orc] minion who is no Orc, the [sauron] Orc, the
		// Uruk-hai or the companion.
		assertEquals(3, scn.GetKeywordCount(grishnakh, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(denizen, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(cavalry, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(halftroll, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(brute, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(tracking, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiMinionWhoIsNoUrukHaiIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var denizen = scn.GetShadowCard("denizen");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, denizen, saruman);

		// Saruman is an [uruk-hai] Wizard: 2 + 1 on Grishnakh, 1 on the Denizen.
		assertEquals(3, scn.GetKeywordCount(grishnakh, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(denizen, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.HUNTER));
	}

	@Test
	public void WhileTheUrukHaiUglukCanBeSpottedEachOrcOrcGainsHunter2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var brute = scn.GetShadowCard("brute");
		var ugluk = scn.GetShadowCard("ugluk");

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, denizen, halftroll, brute, ugluk);

		// 2 + 1 + 1 on Grishnakh, 1 + 1 on the Denizen; Ugluk keeps his own 3.
		assertEquals(4, scn.GetKeywordCount(grishnakh, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(denizen, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(halftroll, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(brute, Keyword.HUNTER));
		assertEquals(3, scn.GetKeywordCount(ugluk, Keyword.HUNTER));
	}

	@Test
	public void TheIsengardUglukIsNoUrukHaiMinionAndGivesNoAdditionalHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var denizen = scn.GetShadowCard("denizen");
		var isengardugluk = scn.GetShadowCard("isengardugluk");
		var tracking = scn.GetShadowCard("tracking");

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, denizen, isengardugluk);

		// The [isengard] Ugluk alone: no [uruk-hai] minion to spot.
		assertEquals(2, scn.GetKeywordCount(grishnakh, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(denizen, Keyword.HUNTER));

		scn.MoveMinionsToTable(tracking);
		// An [uruk-hai] minion beside him, and the Ugluk is not of that culture: hunter 1, not 2.
		assertEquals(3, scn.GetKeywordCount(grishnakh, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(denizen, Keyword.HUNTER));
	}

	@Test
	public void ShadowAbilityExertsGrishnakhTwiceToPlayAnUrukHaiHunterMinionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var tracking = scn.GetShadowCard("tracking");
		var butcher = scn.GetShadowCard("butcher");
		var decimator = scn.GetShadowCard("decimator");
		var breeder = scn.GetShadowCard("breeder");
		var ugluk = scn.GetShadowCard("ugluk");

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh);
		assertInZone(Zone.DECK, tracking, butcher, ugluk, decimator, breeder);
		assertEquals(0, scn.GetWoundsOn(grishnakh));
		scn.SetTwilight(10);
		scn.FreepsPass();

		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(grishnakh));
		scn.ShadowUseCardAction(grishnakh);
		assertEquals(2, scn.GetWoundsOn(grishnakh));

		// The deck is shown first, then the choice: [uruk-hai] hunter minions of three titles, Ugluk among them;
		// not the [uruk-hai] minion who is no hunter or the [orc] hunter minion.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(tracking, butcher, ugluk));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(decimator, breeder));
		scn.ShadowChooseCardBPFromSelection(butcher);

		assertInZone(Zone.SHADOW_CHARACTERS, butcher);
		assertInZone(Zone.DECK, tracking, decimator, breeder);
		// Once as the card leaves the deck that was looked through, once for the definition's own shuffle
		// (CR 4.2 "draw deck").
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoUrukHaiHunterMinionInTheDeckTheAbilityIsPaidForNothingAndTheDeckShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var tracking = scn.GetShadowCard("tracking");
		var butcher = scn.GetShadowCard("butcher");
		var decimator = scn.GetShadowCard("decimator");
		var breeder = scn.GetShadowCard("breeder");
		var ugluk = scn.GetShadowCard("ugluk");
		// Every [uruk-hai] hunter minion of the deck is put in the discard pile.
		scn.MoveCardsToDiscard(tracking, butcher, ugluk);
		assertInDiscard(tracking, butcher, ugluk);

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh);
		assertInZone(Zone.DECK, decimator, breeder);
		assertEquals(0, scn.GetWoundsOn(grishnakh));
		scn.SetTwilight(10);
		scn.FreepsPass();

		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(grishnakh));
		scn.ShadowUseCardAction(grishnakh);
		assertEquals(2, scn.GetWoundsOn(grishnakh));

		// Nothing in the deck can be chosen: the showing is the only decision.
		assertTrue(scn.ShadowHasCardChoiceAvailable(decimator, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(breeder, false));
		scn.ShadowDismissRevealedCards();

		assertInZone(Zone.DECK, decimator, breeder);
		assertInDiscard(tracking, butcher, ugluk);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileGrishnakhCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var tracking = scn.GetShadowCard("tracking");

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh);
		// Vitality 3 with 1 wound: 2 left, so he can exert once but not twice.
		scn.AddWoundsToChar(grishnakh, 1);
		assertEquals(1, scn.GetWoundsOn(grishnakh));
		assertInZone(Zone.DECK, tracking);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grishnakh));
	}
}
