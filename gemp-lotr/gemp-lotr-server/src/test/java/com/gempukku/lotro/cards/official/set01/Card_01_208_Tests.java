package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_208_Tests
{

	// Fellowship sites: Bag End (1) is neither plains nor underground, Ettenmoors (2) is a plains, Rivendell Valley
	// (3) is neither, and Balin's Tomb (4) is underground.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("steed", "1_208");
					// Úlairë Lemenya: [wraith] Nazgûl, strength 9; his text weakens characters bearing [wraith] conditions
					put("lemenya", "1_232");
					// Úlairë Otsëa: [wraith] Nazgûl, strength 9; a Shadow ability, not used
					put("otsea", "1_235");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl, strength 12; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] Orc and no Nazgûl, strength 5; its text needs 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// Nazgûl Sword: a hand weapon for a Nazgûl, strength +2; damage +1 only with 3 burdens
					put("sword", "1_218");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackSteedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Black Steed
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +2
		 * Game Text: Bearer must be a Nazgûl.
		 * While at a plains site, bearer is strength +2. Discard this possession when at an underground site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("steed");

		assertEquals("Black Steed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeANazgulAndAtAPlainsHeIsStrengthPlus2More() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetShadowCard("steed");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(steed);

		scn.StartGame();
		scn.SetTwilight(10);
		assertInHand(steed);
		// To Ettenmoors, a plains site.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));

		// A [wraith] minion who is no Nazgûl cannot bear it.
		scn.MoveMinionsToTable(hound);
		scn.ShadowRefreshDecision();
		assertInHand(steed);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(steed));

		scn.MoveMinionsToTable(lemenya, khamul);
		scn.ShadowRefreshDecision();
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.ShadowPlayAvailable(steed));

		scn.ShadowPlayCard(steed);
		// Nazgûl of two cultures; not the Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertAttachedTo(steed, lemenya);
		// 9 + 2 printed + 2 at a plains; Khamûl, a Nazgûl at the same plains who does not bear it, is unchanged.
		assertEquals(13, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AwayFromAPlainsBearerHasOnlyThePrintedBonusAndAtOneTwoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetShadowCard("steed");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");

		scn.StartGame();
		// Laid early: both Nazgûl are on the table as the fellowship moves.
		scn.MoveMinionsToTable(lemenya, otsea);
		scn.AttachCardsTo(lemenya, steed);

		// Bag End is no plains: the 2 printed on the steed and no more.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertAttachedTo(steed, lemenya);
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(9, scn.GetStrength(otsea));

		// To Ettenmoors, a plains site, which is not underground: the steed stays.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertAttachedTo(steed, lemenya);
		assertEquals(13, scn.GetStrength(lemenya));
		assertEquals(9, scn.GetStrength(otsea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardedWhenTheFellowshipMovesToAnUndergroundSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(lemenya);
		// Another possession beside it on the same Nazgûl.
		scn.AttachCardsTo(lemenya, steed, sword);

		// Rivendell Valley is neither plains nor underground: 9 + 2 + 2 printed.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertAttachedTo(steed, lemenya);
		assertAttachedTo(sword, lemenya);
		assertEquals(13, scn.GetStrength(lemenya));

		// To Balin's Tomb, which is underground.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertInDiscard(steed);
		assertAttachedTo(sword, lemenya);
		assertEquals(11, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtAnUndergroundSiteItIsDiscardedAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetShadowCard("steed");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetTwilight(10);
		// To Balin's Tomb, which is underground.
		scn.FreepsPass();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToHand(steed);
		scn.ShadowRefreshDecision();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertInHand(steed);
		assertEquals(9, scn.GetStrength(lemenya));
		int pool = scn.GetTwilight();

		// Being there is a state, so the steed is discarded as it comes into play (Q-0133), its cost paid.
		assertTrue(scn.ShadowPlayAvailable(steed));
		scn.ShadowPlayCard(steed);
		assertInDiscard(steed);
		assertEquals(0, scn.GetAttachedCards(lemenya).size());
		assertEquals(pool - 1, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
