package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_001_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Its mountains are
	// Erebor 30_55 (site 7, also a river), Smaug's Den 30_56 (site 8, also underground) and Battle of the Five
	// Armies 30_57 (site 9); Esgaroth 30_54 (site 6) is a river and no mountain.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "31_1");
					put("axe2", "31_1");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, no damage bonus; a Fellowship ability not used
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion, no damage bonus; a Maneuver ability not used
					put("dain", "32_2");        // Dain Ironfoot, Lord of the Iron Hills: [dwarven] ally, home 9H (a mountain); his regroup offer wants a follower
					put("bifur", "30_5");       // Bifur, Inarticulate: a [dwarven] follower, no character
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DwarvenAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Dwarven Axe
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [dwarven] character.
		 * 	While bearer is at a mountain site, he is <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Dwarven Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnADwarvenCompanionOrAllyAndOnNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var bifur = scn.GetFreepsCard("bifur");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(bifur);

		scn.StartGame();
		scn.MoveCardsToHand(axe);
		scn.FreepsRefreshDecision();
		assertInHand(axe);
		assertInZone(Zone.SUPPORT, dain, bifur);

		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(axe);
		// A [dwarven] companion and a [dwarven] ally; not Bilbo, a [shire] companion, nor a [dwarven] follower.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, dain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, bifur));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(balin);

		assertAttachedTo(axe, balin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsDamagePlusOneAtEitherMountainSiteAndNotAtARiverThatIsNoMountain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		scn.AttachCardsTo(balin, axe);
		assertAttachedTo(axe, balin);
		assertEquals(0, scn.GetKeywordCount(balin, Keyword.DAMAGE));

		// Esgaroth: a river, no mountain.
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertEquals(0, scn.GetKeywordCount(balin, Keyword.DAMAGE));

		// Erebor: a mountain and a river. Nori, who does not bear the Axe, gains nothing.
		scn.SkipToSite(7);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertAttachedTo(axe, balin);
		assertEquals(1, scn.GetKeywordCount(balin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nori, Keyword.DAMAGE));

		// Smaug's Den: a mountain and underground.
		scn.SkipToSite(8);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetKeywordCount(balin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nori, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyBearerIsDamagePlusOneAtHisMountainHomeSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.AttachCardsTo(balin, axe);
		scn.AttachCardsTo(dain, axe2);
		assertAttachedTo(axe, balin);
		assertAttachedTo(axe2, dain);
		assertEquals(0, scn.GetKeywordCount(dain, Keyword.DAMAGE));

		scn.SkipToSite(8);
		// The move to Battle of the Five Armies (site 9), Dain's home and a mountain: he is at it.
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetKeywordCount(dain, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(balin, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnAllyBearerAwayFromHomeIsNotAtTheFellowshipsMountain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.AttachCardsTo(balin, axe);
		scn.AttachCardsTo(dain, axe2);
		assertAttachedTo(axe, balin);
		assertAttachedTo(axe2, dain);
		assertEquals(0, scn.GetKeywordCount(dain, Keyword.DAMAGE));

		// The fellowship at Erebor, a mountain; Dain's home, site 9, is not yet laid, so he is at no site (CR 4.2
		// "ally": an ally is at his home site).
		scn.SkipToSite(7);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetKeywordCount(balin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(dain, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
