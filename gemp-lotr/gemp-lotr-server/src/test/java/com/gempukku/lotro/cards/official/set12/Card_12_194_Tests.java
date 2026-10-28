package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_194_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability never used
					// Gondorian Merchant: [gondor] Man ally, vitality 1; a maneuver ability never used.  Narsil, Blade of
					// the Faithful ("Bearer must be a [gondor] Man") makes him vitality 2, so that he can exert; its
					// Response answers a wound only.
					put("merchant", "7_97");
					put("narsil", "9_34");

					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6, vitality 2; +3 and fierce only at 2 burdens
					put("chief", "4_20");       // Hill Chief: [dunland] Man, strength 10, vitality 2; a Shadow ability never used
					put("castamir", "8_51");    // Castamir of Umbar: [raider] Man, enduring and fierce, vitality 4; a Shadow ability never used
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, vitality 2; Damage +1 is its whole text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; it is only left here
					put("wold", "12_194");
					// Never laid in these tests.
					put("site3", "18_138");
					put("site4", "17_148");
					put("site5", "11_230");
					put("site6", "13_185");
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
	public void WoldBattlefieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Wold Battlefield
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: Plains. Skirmish: Exert your Man (except an enduring Man) to make him or her damage +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("wold");

		assertEquals("Wold Battlefield", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void EachPlayerMayExertHisOwnManButNotAnEnduringOneToMakeHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var axeman = scn.GetShadowCard("axeman");
		var chief = scn.GetShadowCard("chief");
		var castamir = scn.GetShadowCard("castamir");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		var wold = scn.GetShadowSite("wold");
		scn.ShadowChooseCardBPFromSelection(wold);
		assertSame(wold, scn.GetCurrentSite());
		scn.MoveMinionsToTable(axeman, chief, castamir, guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, axeman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(wold));

		// The Free Peoples player's Men, in the skirmish or not, companion or ally; not his Hobbit, and not the
		// other player's Men.
		scn.FreepsUseCardAction(wold);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, axeman, chief, castamir, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetKeywordCount(axeman, Keyword.DAMAGE));

		// The Shadow player's Men, in the skirmish or not, of two cultures; not his enduring Man, who could exert,
		// not his Uruk-hai, and not the Free Peoples player's Men.
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetKeywordCount(chief, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(wold));
		scn.ShadowUseCardAction(wold);
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, chief));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(castamir, guard, aragorn, boromir, merchant, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axeman);

		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(1, scn.GetKeywordCount(axeman, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(chief, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(chief));
		assertEquals(0, scn.GetWoundsOn(castamir));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DamageBonusLastsForTheSkirmishPhaseItIsUsedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var axeman = scn.GetShadowCard("axeman");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		var wold = scn.GetShadowSite("wold");
		scn.ShadowChooseCardBPFromSelection(wold);
		scn.MoveMinionsToTable(axeman, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, axeman}, new PhysicalCardImpl[]{boromir, brigand});
		scn.FreepsResolveSkirmish(aragorn);

		// In Aragorn's skirmish, Boromir, who skirmishes next, is made damage +1.
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		scn.FreepsUseCardAction(wold);
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		// Still +1 to the end of this skirmish phase.
		scn.ShadowPass();
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.FreepsPass();
		// Aragorn (8) beats the Axeman (6), with no damage bonus of his own: one wound.
		assertEquals(1, scn.GetWoundsOn(axeman));

		// In Boromir's own skirmish phase the bonus is gone.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, brigand));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedToAPlayerWhoseOnlyMenThatCanExertAreEnduring() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var axeman = scn.GetShadowCard("axeman");
		var chief = scn.GetShadowCard("chief");
		var castamir = scn.GetShadowCard("castamir");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		var wold = scn.GetShadowSite("wold");
		scn.ShadowChooseCardBPFromSelection(wold);
		scn.MoveMinionsToTable(axeman, chief, castamir, guard);
		// The Shadow player's Men who are not enduring are exhausted; his enduring Man and his Uruk-hai can exert.
		scn.AddWoundsToChar(axeman, 1);
		scn.AddWoundsToChar(chief, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, axeman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// The Free Peoples player, whose Man can exert, is offered it in this skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(wold));
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(1, scn.GetWoundsOn(chief));
		assertEquals(0, scn.GetWoundsOn(castamir));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wold));
	}
}
