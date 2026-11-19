package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_150_Tests
{

	// The Towers path with Uruk Camp as site 2: site 1 (Eastemnet Gullies) has no terrain, site 2 is a battleground
	// that is also plains, and site 3 (Barrows of Edoras) is a sanctuary.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("decimator", "12_150");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc of strength 13 with no text
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

	// The plain Towers path: White Rocks, site 4, is a battleground and no plains.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("decimator", "12_150");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc of strength 13 with no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("decimator", "12_150");
					put("decimator2", "12_150");
					put("decimator3", "12_150");
					put("tracking", "15_171");  // Tracking Uruk: an [uruk-hai] Uruk-hai whose whole text is Hunter 1
					put("spear", "17_117");     // Spear of the White Hand: [uruk-hai] hand weapon, cost 0, no text but its bearer
					put("sword", "1_160");      // Uruk-hai Sword: [isengard] hand weapon; a trigger on its bearer's win only
					put("bow", "18_119");       // Lurtz's Bow: [uruk-hai] ranged weapon; its texts act only for Lurtz
					put("fury", "18_116");      // Fury of the Evil Army: [uruk-hai] condition on an [uruk-hai] minion; a trigger on its bearer's win only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukDecimatorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Uruk Decimator
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: While this minion is at a battleground site, it is strength +2.<br>While this minion is bearing a possession, it is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("decimator");

		assertEquals("Uruk Decimator", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlusTwoOnlyWhileAtABattlegroundSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var decimator = scn.GetShadowCard("decimator");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(decimator, troop);

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(12, scn.GetStrength(decimator));
		assertEquals(13, scn.GetStrength(troop));

		// The fellowship moves to Uruk Camp, and the minions with it.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(14, scn.GetStrength(decimator));
		// Another minion at the same battleground is not made stronger.
		assertEquals(13, scn.GetStrength(troop));

		// And on to Barrows of Edoras, a sanctuary and no battleground.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(12, scn.GetStrength(decimator));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlusTwoAtABattlegroundThatIsNoPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var decimator = scn.GetShadowCard("decimator");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(decimator, troop);

		assertEquals(12, scn.GetStrength(decimator));
		assertEquals(13, scn.GetStrength(troop));

		// The fellowship moves to White Rocks: Battleground, and nothing else.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(14, scn.GetStrength(decimator));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsDamagePlusOneOnceAPossessionIsPlayedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var decimator = scn.GetShadowCard("decimator");
		var decimator2 = scn.GetShadowCard("decimator2");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(decimator, decimator2);

		assertInZone(Zone.HAND, spear);
		assertEquals(0, scn.GetKeywordCount(decimator, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(decimator2, Keyword.DAMAGE));

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(spear));
		scn.ShadowPlayCard(spear);
		// Either Decimator may bear the Spear.
		assertTrue(scn.ShadowHasCardChoiceAvailable(decimator, decimator2));
		scn.ShadowChooseCard(decimator);

		assertAttachedTo(spear, decimator);
		assertEquals(1, scn.GetKeywordCount(decimator, Keyword.DAMAGE));
		// The copy that bears nothing is not.
		assertEquals(0, scn.GetKeywordCount(decimator2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsDamagePlusOneBearingAPossessionOfAnyCultureOrClassButNotACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var decimator = scn.GetShadowCard("decimator");
		var decimator2 = scn.GetShadowCard("decimator2");
		var decimator3 = scn.GetShadowCard("decimator3");
		var tracking = scn.GetShadowCard("tracking");
		var spear = scn.GetShadowCard("spear");
		var sword = scn.GetShadowCard("sword");
		var bow = scn.GetShadowCard("bow");
		var fury = scn.GetShadowCard("fury");

		scn.StartGame();
		scn.MoveMinionsToTable(decimator, decimator2, decimator3, tracking);
		scn.AttachCardsTo(decimator, sword);
		scn.AttachCardsTo(decimator2, bow);
		scn.AttachCardsTo(decimator3, fury);
		scn.AttachCardsTo(tracking, spear);

		assertAttachedTo(sword, decimator);
		assertAttachedTo(bow, decimator2);
		assertAttachedTo(fury, decimator3);
		assertAttachedTo(spear, tracking);

		// An [isengard] hand weapon and an [uruk-hai] ranged weapon.
		assertEquals(1, scn.GetKeywordCount(decimator, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(decimator2, Keyword.DAMAGE));
		// A condition is no possession.
		assertEquals(0, scn.GetKeywordCount(decimator3, Keyword.DAMAGE));
		// Another Uruk-hai bearing a possession gains nothing from this text.
		assertEquals(0, scn.GetKeywordCount(tracking, Keyword.DAMAGE));
	}
}
