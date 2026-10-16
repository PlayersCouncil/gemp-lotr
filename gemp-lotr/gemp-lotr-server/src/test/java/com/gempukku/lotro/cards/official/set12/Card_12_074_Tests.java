package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_074_Tests
{

	// The Towers path: site 2 is Eastfold 4_331 (plains, no battleground), site 3 Barrows of Edoras 4_337 (neither),
	// site 4 White Rocks 4_346 (battleground, no plains).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider", "12_74");
					put("rider2", "12_74");
					put("corps", "11_73");      // Corps of Harad: [men] Man minion, site 4; fierce at a battleground
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, site 2; his abilities are used only on purpose
					put("bearer", "11_84");     // Harad Standard-bearer: [men] Man minion, cost 4, site 4; a skirmish ability used only on purpose
					put("goaded", "12_67");     // Goaded to War: a [men] condition in the support area; adds (1) at a move to a battleground or plains
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion, no text
					put("dagger", "18_69");     // Henchman's Dagger: a [men] possession on a [men] minion
					put("poison", "8_28");      // Spider Poison: a [gollum] possession that lies in its owner's support area
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant is his whole text
					put("chain", "4_278");      // Heavy Chain: a [rohan] possession on a Man
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion; Knight is his whole text
					// Narsil, Blade of the Faithful: a [gondor] artifact on a [gondor] Man; its response is used only on purpose
					put("narsil", "9_34");
					put("wounded", "4_215");    // Wounded: an [isengard] condition on a Free Peoples Man, cheated on
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MumakRiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Mûmak Rider
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Archer</b>.
		 * 	When you play this minion to a battleground or plains site, you may spot another [men] minion to discard a possession from play.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rider");

		assertEquals("Mûmak Rider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedAtAPlainsSiteWithAnotherMenMinionItMayDiscardAPossessionFromPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var corps = scn.GetShadowCard("corps");
		var dagger = scn.GetShadowCard("dagger");
		var poison = scn.GetShadowCard("poison");
		var wounded = scn.GetShadowCard("wounded");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var knight = scn.GetFreepsCard("knight");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(guard, knight);
		scn.AttachCardsTo(guard, chain);
		// An artifact and a condition, neither of them a possession.
		scn.AttachCardsTo(knight, narsil, wounded);

		scn.StartGame();
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, dagger);
		scn.MoveCardsToSupportArea(poison);
		scn.MoveCardsToHand(rider);
		scn.FreepsPass();

		// Shadow phase at Eastfold, a plains site.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(rider);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rider));
		scn.ShadowAcceptOptionalTrigger();

		// Possessions of both sides, borne and in a support area; not the artifact, the condition or The One Ring.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chain, dagger, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(narsil, wounded, scn.GetRing()));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chain);

		assertInDiscard(chain);
		assertAttachedTo(dagger, corps);
		assertInZone(Zone.SUPPORT, poison);
		assertAttachedTo(narsil, knight);
		assertAttachedTo(wounded, knight);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtABattlegroundSiteItMayDiscardAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var ted = scn.GetShadowCard("ted");
		var dagger = scn.GetShadowCard("dagger");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, chain);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(ted);
		scn.AttachCardsTo(ted, dagger);
		scn.SetShadowHand(rider);
		scn.FreepsPass();

		// Shadow phase at White Rocks, a battleground site.  The other [men] minion is a Hobbit.
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(rider);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rider));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(chain, dagger));
		scn.ShadowChooseCard(dagger);

		assertInDiscard(dagger);
		assertAttachedTo(chain, guard);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtASiteThatIsNeitherABattlegroundNorPlainsItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var corps = scn.GetShadowCard("corps");
		var dagger = scn.GetShadowCard("dagger");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, chain);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, dagger);
		scn.SetShadowHand(rider);
		scn.FreepsPass();

		// Shadow phase at Barrows of Edoras, neither a battleground nor plains.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(rider);

		assertInZone(Zone.SHADOW_CHARACTERS, rider);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rider));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertAttachedTo(chain, guard);
		assertAttachedTo(dagger, corps);
	}

	@Test
	public void WithNoOtherMenMinionItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var goaded = scn.GetShadowCard("goaded");
		var brigand = scn.GetShadowCard("brigand");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, chain);

		scn.StartGame();
		// A [men] card that is no minion, and a Man minion who is not [men].
		scn.MoveCardsToSupportArea(goaded);
		scn.MoveMinionsToTable(brigand);
		scn.MoveCardsToHand(rider);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(rider);

		assertInZone(Zone.SHADOW_CHARACTERS, rider);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rider));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertAttachedTo(chain, guard);
	}

	@Test
	public void WithNoPossessionInPlayItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var corps = scn.GetShadowCard("corps");
		var wounded = scn.GetShadowCard("wounded");
		var knight = scn.GetFreepsCard("knight");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(knight);
		// An artifact and a condition, and no possession.
		scn.AttachCardsTo(knight, narsil, wounded);

		scn.StartGame();
		scn.MoveMinionsToTable(corps);
		scn.MoveCardsToHand(rider);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(rider);

		assertInZone(Zone.SHADOW_CHARACTERS, rider);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rider));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertAttachedTo(narsil, knight);
		assertAttachedTo(wounded, knight);
	}

	@Test
	public void TheDiscardMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var corps = scn.GetShadowCard("corps");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, chain);

		scn.StartGame();
		scn.MoveMinionsToTable(corps);
		scn.MoveCardsToHand(rider);
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(rider);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rider));
		scn.ShadowDeclineOptionalTrigger();

		assertAttachedTo(chain, guard);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyItsOwnPlaySetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var rider2 = scn.GetShadowCard("rider2");
		var bearer = scn.GetShadowCard("bearer");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, chain);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.MoveCardsToHand(bearer, rider2);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		// Another [men] minion played, with this one in play at a plains site: nothing is offered.
		scn.ShadowPlayCard(bearer);
		assertInZone(Zone.SHADOW_CHARACTERS, bearer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rider));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertAttachedTo(chain, guard);

		// A second copy played: its own offer, and none from the copy already in play.
		scn.ShadowPlayCard(rider2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rider2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rider));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(chain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
