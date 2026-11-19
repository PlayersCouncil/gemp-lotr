package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_044_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The skirmishes of
	// turn 1 are at The Troll Hoard (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "32_44");
					// Sauron, The Necromancer: [sauron] Maia minion, 13/4, the one Sauron of the Hobbit sets.  Each
					// [gundabad] Orc is +1 and fierce (none is laid); an assignment ability, not used.
					put("sauron", "32_45");
					// Úlairë Nelya, Revived: [wraith] Nazgûl, 10/3, printed fierce; damage +1 only while a companion
					// bears a follower (none is laid).
					put("nelya", "32_63");
					// Úlairë Lemenya, Revived: [wraith] Nazgûl, 9/2, printed fierce; a site-number text and a trigger
					// on a condition discarded by the Free Peoples player.
					put("lemenya", "32_62");
					// Orkish Veteran: [sauron] Orc minion who is no Sauron, 7/2; an archer text at site 9 and a
					// regroup ability.
					put("veteran", "32_43");
					// He Is Summoning His Servants: a [wraith] condition (no minion) in the support area; each
					// [wraith] minion is strength +2 while 2 Wise characters cannot be spotted, and a Shadow ability.
					put("summoning", "32_58");

					// Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability.
					put("balin", "30_2");
					// Nori, Of the Blue Mountains: [dwarven] Dwarf companion, 7/3; a maneuver ability.
					put("nori", "30_15");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void RingofThrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Ring of Thrór, Last of the Seven Rings
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: +1
		 * Game Text: Bearer must be Sauron.<br>He is <b>fierce</b>.<br><b>Skirmish:</b> Remove (2) to make Sauron or a [wraith] minion <b>damage +1</b> (limit +1).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ring");

		assertEquals("Ring of Thrór", card.getBlueprint().getTitle());
		assertEquals("Last of the Seven Rings", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnlyOnSauron() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var sauron = scn.GetShadowCard("sauron");
		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		scn.MoveMinionsToTable(veteran);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCardsToHand(ring);
		scn.ShadowRefreshDecision();
		assertInHand(ring);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);

		// A [sauron] minion who is no Sauron: no bearer.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(ring));

		scn.MoveMinionsToTable(sauron);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(ring));
		scn.ShadowPlayCard(ring);

		// Sauron is the one bearer, and nothing is asked.
		assertAttachedTo(ring, sauron);
		assertTrue(scn.GetAttachedCards(veteran).isEmpty());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SauronIsFierceWhileHeBearsTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var sauron = scn.GetShadowCard("sauron");
		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		scn.MoveMinionsToTable(sauron, veteran);
		assertInZone(Zone.SHADOW_CHARACTERS, sauron, veteran);

		assertFalse(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(veteran, Keyword.FIERCE));

		scn.AttachCardsTo(sauron, ring);
		assertAttachedTo(ring, sauron);

		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		// Another minion beside him is not.
		assertFalse(scn.HasKeyword(veteran, Keyword.FIERCE));
	}

	@Test
	public void SkirmishActionRemovesTwoToMakeSauronDamagePlus1AndUsedOnHimAgainInTheSameSkirmishIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var sauron = scn.GetShadowCard("sauron");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var veteran = scn.GetShadowCard("veteran");
		var summoning = scn.GetShadowCard("summoning");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(sauron, nelya, lemenya, veteran);
		scn.MoveCardsToSupportArea(summoning);
		scn.AttachCardsTo(sauron, ring);
		assertAttachedTo(ring, sauron);
		assertInZone(Zone.SUPPORT, summoning);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, sauron);
		// Nelya is put on Balin beside Sauron; Lemenya and the Veteran are left unassigned.
		scn.ShadowAssignToMinions(balin, nelya);
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ring));

		scn.ShadowUseCardAction(ring);

		assertEquals(2, scn.GetTwilight());
		// Sauron, a [wraith] minion in the skirmish and one in no skirmish; not a [sauron] minion of another
		// title, nor a [wraith] card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sauron, nelya, lemenya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(veteran, summoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sauron);
		assertEquals(1, scn.GetKeywordCount(sauron, Keyword.DAMAGE));

		// At the limit the action stays on offer: used again on Sauron, the (2) is paid and he stays at +1.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(ring));
		scn.ShadowUseCardAction(ring);
		assertEquals(0, scn.GetTwilight());
		scn.ShadowChooseCard(sauron);

		assertEquals(1, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtTheLimitAUseOnAWraithMinionAfterSauronsPlus1GivesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var sauron = scn.GetShadowCard("sauron");
		var nelya = scn.GetShadowCard("nelya");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(sauron, nelya);
		scn.AttachCardsTo(sauron, ring);
		assertAttachedTo(ring, sauron);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, sauron);
		scn.ShadowAssignToMinions(balin, nelya);
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetTwilight());

		scn.ShadowUseCardAction(ring);
		scn.ShadowChooseCard(sauron);
		assertEquals(1, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));

		// CR 4.2 "limit": the limit applies to the card for the whole phase, whoever the bonus went to.
		scn.FreepsPass();
		scn.ShadowUseCardAction(ring);
		scn.ShadowChooseCard(nelya);

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsTheSkirmishAndTheLimitRenewsInTheNextWhereAWraithMinionIsMadeDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var sauron = scn.GetShadowCard("sauron");
		var nelya = scn.GetShadowCard("nelya");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		scn.MoveMinionsToTable(sauron, nelya);
		scn.AttachCardsTo(sauron, ring);
		assertAttachedTo(ring, sauron);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, sauron},
				new PhysicalCardImpl[]{nori, nelya});
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetKeywordCount(sauron, Keyword.DAMAGE));

		scn.ShadowUseCardAction(ring);
		scn.ShadowChooseCard(sauron);
		assertEquals(2, scn.GetTwilight());

		// Still in force at the decision that follows, in the same skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(sauron, Keyword.DAMAGE));

		// Balin, 7, loses to Sauron, 13, and is not overwhelmed: 1 wound and 1 for the damage bonus.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(balin));

		scn.FreepsResolveSkirmish(nori);
		// Nori's skirmish is a phase of its own: Sauron's +1 is gone.
		assertEquals(0, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(ring));
		scn.ShadowUseCardAction(ring);
		scn.ShadowChooseCard(nelya);

		// The limit is spent anew in this skirmish, on a [wraith] minion.
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWithOnlyOneTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var sauron = scn.GetShadowCard("sauron");
		var nelya = scn.GetShadowCard("nelya");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(sauron, nelya);
		scn.AttachCardsTo(sauron, ring);
		assertAttachedTo(ring, sauron);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, sauron);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ring));
		assertEquals(0, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
	}
}
