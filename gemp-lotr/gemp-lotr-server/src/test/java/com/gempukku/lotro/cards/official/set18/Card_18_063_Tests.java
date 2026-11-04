package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halberd", "18_63");

					put("corps", "11_73");      // Corps of Harad: [men] Man, strength 10, vitality 3; fierce only at a battleground (none on this path)
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; abilities used only on purpose
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion with no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion for the dead pile
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] Dwarf companion for the dead pile
					put("guard", "1_7");        // Dwarf Guard: a companion; the Shadow player's own copy lies in his own dead pile
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a companion in the discard pile
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ALLY for the dead pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCompanionsInTheDeadPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halberd", "18_63");
					// Ruffian: a [men] minion to bear it; his text is about his own strength
					put("ruffian", "18_72");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion for the dead pile
					put("gimli", "1_13");       // Gimli, Son of Gloin: another
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairHalberdStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Corsair Halberd
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [men] minion.
		 * 	Bearer is <b>damage +1</b>.
		 * 	<b>Shadow:</b> Remove a companion in the dead pile from the game to control a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("halberd");

		assertEquals("Corsair Halberd", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAMenMinionAndIsThenDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halberd = scn.GetShadowCard("halberd");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveCardsToHand(halberd);
		scn.MoveMinionsToTable(corps, ted, brigand);
		scn.SetTwilight(5);
		assertInZone(Zone.HAND, halberd);
		scn.FreepsPass();

		assertEquals(0, scn.GetKeywordCount(corps, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(halberd));
		scn.ShadowPlayCard(halberd);
		// [men] minions of two races are offered; a Man minion of another culture is not, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(corps);

		assertAttachedTo(halberd, corps);
		assertEquals(1, scn.GetKeywordCount(corps, Keyword.DAMAGE));
		// Ted Sandyman, another [men] minion, bears nothing and gains nothing.
		assertEquals(0, scn.GetKeywordCount(ted, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyACompanionInTheFreePeoplesDeadPileCanBeRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halberd = scn.GetShadowCard("halberd");
		var corps = scn.GetShadowCard("corps");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var merry = scn.GetFreepsCard("merry");
		var shadowGuard = scn.GetShadowCard("guard");
		// A companion and an ally in the Free Peoples dead pile, a companion in his discard pile, and a companion in
		// the Shadow player's own dead pile, killed on his own turn.
		scn.MoveCardsToDeadPile(aragorn, gimli, thrarin, shadowGuard);
		scn.MoveCardsToDiscard(merry);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, halberd);
		assertInZone(Zone.DEAD, aragorn, gimli, thrarin, shadowGuard);
		assertInZone(Zone.DISCARD, merry);
		assertAttachedTo(halberd, corps);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(halberd));
		scn.ShadowUseCardAction(halberd);
		// The cards are shown, and read by blueprint: no valid candidate shares a title with an invalid one.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(thrarin, shadowGuard, merry));
		scn.ShadowChooseCardBPFromSelection(aragorn);

		assertInZone(Zone.REMOVED, aragorn);
		assertInZone(Zone.DEAD, gimli, thrarin, shadowGuard);
		assertInZone(Zone.DISCARD, merry);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoCompanionInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halberd = scn.GetShadowCard("halberd");
		var corps = scn.GetShadowCard("corps");
		var thrarin = scn.GetFreepsCard("thrarin");
		var merry = scn.GetFreepsCard("merry");
		var shadowGuard = scn.GetShadowCard("guard");
		scn.MoveCardsToDeadPile(thrarin, shadowGuard);
		scn.MoveCardsToDiscard(merry);

		scn.StartGame();
		// Site 1 is there to be taken.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, halberd);
		assertInZone(Zone.DEAD, thrarin, shadowGuard);
		assertInZone(Zone.DISCARD, merry);
		assertAttachedTo(halberd, corps);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(halberd));
		assertFalse(scn.IsSiteControlled(site1));
	}

	@Test
	public void WithNoSiteThatCanBeControlledTheAbilityIsStillUsedAndTheCompanionRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halberd = scn.GetShadowCard("halberd");
		var corps = scn.GetShadowCard("corps");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToDeadPile(aragorn);

		// Player 1's first turn: the other marker is still on site 1, so no site can be controlled.
		scn.StartGame();
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, halberd);
		assertInZone(Zone.DEAD, aragorn);
		assertAttachedTo(halberd, corps);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(halberd));
		scn.ShadowUseCardAction(halberd);

		assertInZone(Zone.REMOVED, aragorn);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovesACompanionInTheDeadPileFromTheGameToControlASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCompanionsInTheDeadPileScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToDeadPile(aragorn, gimli);
		assertInZone(Zone.DEAD, aragorn, gimli);

		var halberd = scn.GetShadowCard("halberd");
		var ruffian = scn.GetShadowCard("ruffian");

		// Both markers are at site 2 when the fellowship moves on to site 3: site 1 can be controlled.
		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(ruffian);
		scn.AttachCardsTo(ruffian, halberd);
		assertAttachedTo(halberd, ruffian);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DEAD, aragorn, gimli);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.ShadowActionAvailable(halberd));

		// One of the two dead companions is the price.
		scn.ShadowUseCardAction(halberd);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(gimli);
		assertInZone(Zone.REMOVED, gimli);
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.IsSiteControlled(site1));
		assertAttachedTo(halberd, ruffian);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
