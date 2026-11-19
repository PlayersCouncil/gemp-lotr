package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("threat", "13_103");
					put("threat2", "13_103");

					// [orc] conditions, one in the support area and one on a site
					put("depths", "13_120");    // Unforgiving Depths: support area, cost 0; muster at 3 underground sites, a Shadow ability used only on purpose
					put("blow", "15_115");      // Pummeling Blow: plays on the fellowship's current site (cheated there); Trolls +1
					// Cards beside them that are no [orc] condition
					put("fear", "1_278");       // Strength Born of Fear: [sauron] support-area condition, a skirmish ability used only on purpose
					put("goblin", "11_137");    // Scurrying Goblin: [orc] Orc minion; its trigger answers its own play only
					put("spear", "12_96");      // Orc Spear: [orc] possession, borne by the Goblin

					// Minions for the skirmish ability
					put("prowler", "11_136");   // Prowling Orc: [orc] Orc lurker, strength 6; a skirmish ability used only on purpose
					put("column", "11_72");     // Column of Easterlings: [men] Man lurker, strength 8; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, no lurker; "When you play" only, and he is cheated in

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRedhornScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("threat", "13_103");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Redhorn Pass: a Shadows-block mountain, laid by MoveCardToAdventurePath as site 2; "Culture tokens
					// cannot be added, removed, or reinforced."
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AlwaysThreateningStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Always Threatening
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this, you may add an [orc] token here for each [orc] condition you can spot.<br><b>Skirmish:</b> Discard this from play or remove 2 tokens from here to make a lurker minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("threat");

		assertEquals("Always Threatening", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedAloneItSpotsItselfForOneTokenAndAnotherOrcConditionPlayedLaterAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threat = scn.GetShadowCard("threat");
		var depths = scn.GetShadowCard("depths");
		scn.MoveCardsToHand(threat, depths);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(threat));
		scn.ShadowPlayCard(threat);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(threat));
		scn.ShadowAcceptOptionalTrigger();

		// The only [orc] condition is this one.
		assertEquals(1, scn.GetCultureTokensOn(threat));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [orc] condition played afterwards does not set it off again.
		scn.ShadowPlayCard(depths);
		assertInZone(Zone.SUPPORT, depths);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(threat));
		assertEquals(1, scn.GetCultureTokensOn(threat));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TakesATokenForEachOrcConditionInPlayWhereverItIsAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threat = scn.GetShadowCard("threat");
		var depths = scn.GetShadowCard("depths");
		var blow = scn.GetShadowCard("blow");
		var threat2 = scn.GetShadowCard("threat2");
		var fear = scn.GetShadowCard("fear");
		var goblin = scn.GetShadowCard("goblin");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(threat);
		scn.MoveCardsToSupportArea(depths, fear, threat2);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		scn.AttachCardsTo(goblin, spear);
		assertAttachedTo(spear, goblin);
		assertInZone(Zone.SUPPORT, depths, fear, threat2);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// Pummeling Blow on the site the fellowship has just moved to.
		scn.AttachCardsTo(scn.GetCurrentSite(), blow);
		assertAttachedTo(blow, scn.GetCurrentSite());
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(threat);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(threat));
		scn.ShadowAcceptOptionalTrigger();

		// Itself, the other copy, Unforgiving Depths and Pummeling Blow: not the [sauron] condition, the [orc]
		// possession or the [orc] minion. Every one that can be spotted, with nothing asked. The tokens go on this
		// card alone, not on the other copy nor on the other [orc] conditions.
		assertEquals(4, scn.GetCultureTokensOn(threat));
		assertEquals(0, scn.GetCultureTokensOn(threat2));
		assertEquals(0, scn.GetCultureTokensOn(depths));
		assertEquals(0, scn.GetCultureTokensOn(blow));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threat = scn.GetShadowCard("threat");
		var depths = scn.GetShadowCard("depths");
		scn.MoveCardsToHand(threat);
		scn.MoveCardsToSupportArea(depths);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, depths);
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(threat);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(threat));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, threat);
		assertEquals(0, scn.GetCultureTokensOn(threat));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhereNoTokenCanBeAddedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornScenario();

		var threat = scn.GetShadowCard("threat");
		var redhorn = scn.GetShadowSite("redhorn");
		scn.MoveCardsToHand(threat);

		scn.StartGame();
		scn.MoveCardToAdventurePath(redhorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertSame(redhorn, scn.GetCurrentSite());
		scn.ShadowPlayCard(threat);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(threat));
		assertInZone(Zone.SUPPORT, threat);
		assertEquals(0, scn.GetCultureTokensOn(threat));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFewerThan2TokensItIsDiscardedToMakeALurkerOfAnyCultureStrengthPlus2ForThisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var threat = scn.GetShadowCard("threat");
		var threat2 = scn.GetShadowCard("threat2");
		var prowler = scn.GetShadowCard("prowler");
		var column = scn.GetShadowCard("column");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(threat, threat2);
		// The other copy holds 2 tokens; this one holds none.
		scn.AddTokensToCard(threat2, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, threat, threat2);
		assertEquals(0, scn.GetCultureTokensOn(threat));
		assertEquals(2, scn.GetCultureTokensOn(threat2));
		scn.MoveMinionsToTable(prowler, column, runner);
		scn.SkipToAssignments();
		// The Runner in the first skirmish, the Prowling Orc (a lurker) in a later one, the Column assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{sam, prowler});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(threat));
		scn.ShadowUseCardAction(threat);

		// The discard is the only way to pay, and is taken with nothing asked; the other copy keeps its tokens.
		assertInDiscard(threat);
		assertInZone(Zone.SUPPORT, threat2);
		assertEquals(2, scn.GetCultureTokensOn(threat2));

		// A lurker of either culture and race, in a later skirmish or in none; not the minion who is no lurker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(prowler, column));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(6, scn.GetStrength(prowler));
		scn.ShadowChooseCard(prowler);

		assertEquals(8, scn.GetStrength(prowler));
		assertEquals(8, scn.GetStrength(column));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// In the Prowling Orc's own skirmish, a phase of its own, the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(sam);
		assertEquals(6, scn.GetStrength(prowler));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithBothCostsPayableThePlayerChoosesAndRemoving2TokensKeepsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var threat = scn.GetShadowCard("threat");
		var prowler = scn.GetShadowCard("prowler");
		var column = scn.GetShadowCard("column");
		scn.MoveCardsToSupportArea(threat);
		scn.AddTokensToCard(threat, 3);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, threat);
		assertEquals(3, scn.GetCultureTokensOn(threat));
		scn.MoveMinionsToTable(prowler, column);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, prowler);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(threat));
		scn.ShadowUseCardAction(threat);

		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Discard from play"));
		assertTrue(scn.ShadowChoiceAvailable("Remove 2 tokens"));
		scn.ShadowChooseOption("Remove 2 tokens");

		assertInZone(Zone.SUPPORT, threat);
		assertEquals(1, scn.GetCultureTokensOn(threat));

		assertEquals(6, scn.GetStrength(prowler));
		scn.ShadowChooseCard(prowler);
		assertEquals(8, scn.GetStrength(prowler));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoLurkerInPlayItIsStillUsedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var threat = scn.GetShadowCard("threat");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(threat);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, threat);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(threat));
		scn.ShadowUseCardAction(threat);

		assertInDiscard(threat);
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var threat = scn.GetShadowCard("threat");
		var prowler = scn.GetShadowCard("prowler");
		scn.MoveCardsToSupportArea(threat);
		scn.AddTokensToCard(threat, 2);

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(threat));
		scn.MoveMinionsToTable(prowler);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(threat));
	}
}
