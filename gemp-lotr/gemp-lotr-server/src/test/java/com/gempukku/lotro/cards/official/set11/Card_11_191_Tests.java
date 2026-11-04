package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_191_Tests
{

	// The Towers path: site 1 (Eastemnet Gullies) has no terrain, site 2 (Eastfold) is a plains and no battleground,
	// site 4 (White Rocks) a battleground and no plains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "11_191");
					put("swarming", "11_203");  // Swarming Uruk: an [uruk-hai] Uruk-hai, strength 9; cannot take wounds
					                            // while another [uruk-hai] minion is assigned, which none is here
					put("ruthless", "11_200");  // Ruthless Uruk: an [uruk-hai] Uruk-hai; its text answers only its own assignment
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard;
					                            // each companion resistance -1, a skirmish ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai; damage +1 and nothing else
					put("alliance", "12_141");  // Dark Alliance: an [uruk-hai] condition; a response used only on purpose
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path with Uruk Camp as site 2: a battleground and a plains.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "11_191");
					put("swarming", "11_203");  // Swarming Uruk: an [uruk-hai] Uruk-hai, strength 9
					put("ruthless", "11_200");  // Ruthless Uruk: an [uruk-hai] Uruk-hai; its text answers only its own assignment
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground. Plains. Exerts each Uruk-hai played; none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardSiegeBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Isengard Siege Bow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Vitality: +1
		 * Game Text: Bearer must be an [uruk-hai] minion.<br>While bearer is at a battleground site, it is an <b>archer</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Isengard Siege Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedOnAnUrukHaiCultureMinionOfAnyRaceAndNotOnAnotherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var swarming = scn.GetShadowCard("swarming");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var alliance = scn.GetShadowCard("alliance");
		scn.MoveCardsToHand(bow);
		scn.MoveCardsToSupportArea(alliance);

		scn.StartGame();
		assertInHand(bow);
		scn.MoveMinionsToTable(swarming, saruman, savage);
		scn.FreepsPass();

		assertInHand(bow);
		assertTrue(scn.ShadowPlayAvailable(bow));
		scn.ShadowPlayCard(bow);

		// An [uruk-hai] Uruk-hai and an [uruk-hai] Wizard; not an [isengard] Uruk-hai, nor an [uruk-hai] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(swarming, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, alliance, scn.GetRingBearer()));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saruman);

		assertAttachedTo(bow, saruman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsAnArcherOnlyOnceTheFellowshipIsAtABattlegroundSiteAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var bow = scn.GetShadowCard("bow");
		var swarming = scn.GetShadowCard("swarming");
		var ruthless = scn.GetShadowCard("ruthless");

		scn.StartGame();
		// Laid early, in the fellowship phase at site 1, to be read before and after the move.
		scn.MoveMinionsToTable(swarming, ruthless);
		scn.AttachCardsTo(swarming, bow);

		assertAttachedTo(bow, swarming);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(swarming, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(ruthless, Keyword.ARCHER));

		// The fellowship moves to Uruk Camp.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(swarming, Keyword.ARCHER));
		// Another [uruk-hai] Uruk-hai at the same battleground, bearing no bow, is no archer.
		assertFalse(scn.HasKeyword(ruthless, Keyword.ARCHER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsNoArcherAtAPlainsThatIsNoBattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var swarming = scn.GetShadowCard("swarming");

		scn.StartGame();
		scn.MoveMinionsToTable(swarming);
		scn.AttachCardsTo(swarming, bow);
		assertAttachedTo(bow, swarming);
		assertFalse(scn.HasKeyword(swarming, Keyword.ARCHER));

		// The fellowship moves to Eastfold.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(swarming, Keyword.ARCHER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsAnArcherAtABattlegroundThatIsNoPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var swarming = scn.GetShadowCard("swarming");
		var ruthless = scn.GetShadowCard("ruthless");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(swarming, ruthless);
		scn.AttachCardsTo(swarming, bow);

		// White Rocks.
		assertAttachedTo(bow, swarming);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(swarming, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(ruthless, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
