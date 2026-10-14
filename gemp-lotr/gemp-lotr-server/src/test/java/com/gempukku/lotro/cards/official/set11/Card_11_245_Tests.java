package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_245_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8
					put("sword", "1_112");        // Ranger's Sword: a possession, hand weapon, strength +2, on Aragorn
					put("merchant", "7_97");      // Gondorian Merchant: a [gondor] Man ALLY, strength 6
					put("narsil", "9_34");        // Narsil, Blade of the Faithful: an ARTIFACT hand weapon, no strength of its own (he is no Elendil or Isildur)
					put("legolas", "1_50");       // Legolas, Greenleaf: strength 6
					put("bow", "1_41");           // Elven Bow: a RANGED weapon, no strength of its own
					put("ted", "18_75");          // Ted Sandyman, Chief's Men's Ally: a [men] minion, strength 3; abilities used only on purpose
					put("dagger", "18_69");       // Henchman's Dagger: a Shadow hand weapon, strength +1
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");       // Fangorn Glade: its text answers a move to it; the game starts there
					put("gate", "11_245");
					put("ruins", "18_138");       // Sirannon Ruins: no text
					// Never laid in these tests.
					put("site4", "11_234");
					put("site5", "17_148");
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
	public void HelmsGateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Helm's Gate
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: Battleground. Each character bearing a hand weapon is strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("gate");

		assertEquals("Helm's Gate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void EachCharacterBearingAHandWeaponIsStrengthPlusTwoThereAndNoOtherCharacterIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var ted = scn.GetShadowCard("ted");
		var dagger = scn.GetShadowCard("dagger");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(merchant, narsil);
		scn.AttachCardsTo(legolas, bow);

		scn.StartGame(scn.GetFreepsSite("site1"));

		// At Fangorn Glade.
		assertEquals(10, scn.GetStrength(aragorn));    // 8 + 2 for the sword
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(4, scn.GetStrength(frodo));       // 3 + 1 for The Ruling Ring

		scn.FreepsPass();
		var gate = scn.GetShadowSite("gate");
		scn.ShadowChooseCardBPFromSelection(gate);
		assertSame(gate, scn.GetCurrentSite());

		scn.MoveMinionsToTable(ted);
		scn.AttachCardsTo(ted, dagger);

		// A companion with a possession hand weapon, an ally with an artifact one, the Shadow player's minion with his own.
		assertEquals(12, scn.GetStrength(aragorn));    // 8 + 2 + 2
		assertEquals(8, scn.GetStrength(merchant));    // 6 + 2
		assertEquals(6, scn.GetStrength(ted));         // 3 + 1 for the dagger + 2
		// A ranged weapon, and an item that is no weapon (the Ring), are no hand weapon.
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromHelmsGateTheBonusIsGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(merchant, narsil);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		var gate = scn.GetShadowSite("gate");
		scn.ShadowChooseCardBPFromSelection(gate);

		scn.SkipToMovementDecision();
		assertSame(gate, scn.GetCurrentSite());
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(merchant));

		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("ruins"));

		assertEquals("Sirannon Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(10, scn.GetStrength(aragorn));    // 8 + 2 for the sword
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
