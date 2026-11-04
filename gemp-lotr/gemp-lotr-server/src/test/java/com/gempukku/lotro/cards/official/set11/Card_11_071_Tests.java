package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bold", "11_71");
					// Corps of Harad: [men] Man minion, vitality 3; fierce only at a battleground, and this path has none
					put("corps", "11_73");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, vitality 4; his abilities
					// are used only on purpose
					put("ted", "18_75");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc of vitality 3 with no text
					put("soldier", "4_254");    // Southron Soldier: a [raider] Man minion; Southron and ambush (2) only
					// Goaded to War: a [men] condition in the support area; its trigger answers a move to a
					// battleground or plains, and no move is made after it is laid
					put("goaded", "12_67");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8; a maneuver ability
					put("boromir", "1_97");     // Boromir, Son of Denethor: a Free Peoples Man to bear Wounded
					put("chain", "4_278");      // Heavy Chain: a Free Peoples possession (armor) on a Man; an overwhelm bar only
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: an artifact (no possession) on a [gondor] Man
					put("dagger", "18_69");     // Henchman's Dagger: a [men] possession borne by a [men] minion; +1 strength
					// Spider Poison: a [gollum] possession in the Shadow support area; nothing it can be transferred to
					put("poison", "8_28");
					put("wounded", "4_215");    // Wounded: a Shadow condition on a Free Peoples Man; it bars heals only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoldandCunningStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Bold and Cunning
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Toil 2</b>. <helper>(For each [men] character you exert when playing this, its twilight cost is -2.)</helper><br>Spot a [men] minion to discard a possession from play.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("bold");

		assertEquals("Bold and Cunning", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAMenManItDiscardsAPossessionFromPlayOfEitherSideBorneOrNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetShadowCard("bold");
		var corps = scn.GetShadowCard("corps");
		var dagger = scn.GetShadowCard("dagger");
		var poison = scn.GetShadowCard("poison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var chain = scn.GetFreepsCard("chain");
		var narsil = scn.GetFreepsCard("narsil");
		var wounded = scn.GetShadowCard("wounded");
		var ring = scn.GetRing();
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, chain, narsil);
		scn.MoveCardsToHand(bold);

		scn.StartGame();
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, dagger);
		scn.AttachCardsTo(boromir, wounded);
		scn.MoveCardsToSupportArea(poison);
		assertAttachedTo(chain, aragorn);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(dagger, corps);
		assertAttachedTo(wounded, boromir);
		assertInZone(Zone.SUPPORT, poison);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, corps);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(bold));
		scn.ShadowPlayCard(bold);
		// The pool pays in full: toil asks for none, and none is exerted.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(corps));

		// Possessions of both sides, borne by a companion, by a minion and by nobody; not an artifact, a condition
		// or The One Ring.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chain, dagger, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(narsil, wounded, ring));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chain);

		assertInDiscard(chain);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(dagger, corps);
		assertAttachedTo(wounded, boromir);
		assertInZone(Zone.SUPPORT, poison);
		assertInDiscard(bold);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMenMinionWhoIsNoManIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetShadowCard("bold");
		var ted = scn.GetShadowCard("ted");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, chain);
		scn.MoveCardsToHand(bold);

		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		assertAttachedTo(chain, aragorn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, ted);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(bold));
		scn.ShadowPlayCard(bold);
		scn.ShadowDeclineChoosing();

		// One possession in play: it is discarded without a choice.
		assertInDiscard(chain);
		assertInDiscard(bold);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyAMenConditionAndAManMinionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetShadowCard("bold");
		var soldier = scn.GetShadowCard("soldier");
		var goaded = scn.GetShadowCard("goaded");
		var aragorn = scn.GetFreepsCard("aragorn");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, chain);
		scn.MoveCardsToHand(bold);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.MoveCardsToSupportArea(goaded);
		assertAttachedTo(chain, aragorn);
		assertInHand(bold);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		// The Soldier's ambush is declined.
		scn.ShadowDeclineOptionalTrigger();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(bold));
		assertInHand(bold);
		assertAttachedTo(chain, aragorn);
	}

	@Test
	public void WithNoPossessionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetShadowCard("bold");
		var corps = scn.GetShadowCard("corps");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var narsil = scn.GetFreepsCard("narsil");
		var wounded = scn.GetShadowCard("wounded");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, narsil);
		scn.MoveCardsToHand(bold);

		scn.StartGame();
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(boromir, wounded);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(wounded, boromir);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, corps);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(bold));
		scn.ShadowPlayCard(bold);
		scn.ShadowDeclineChoosing();

		assertInDiscard(bold);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(wounded, boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachMenCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetShadowCard("bold");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, chain);
		scn.MoveCardsToHand(bold);

		scn.StartGame();
		scn.MoveMinionsToTable(corps, ted, troop);
		assertAttachedTo(chain, aragorn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, corps);
		// Ted and the Troop stay out of any skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 5 - 2 - 2 is the least it can cost, with both [men] minions exerted; the [moria] Troop does not count.
		// Toil 1 would want 3, and toil 3 (or the Troop counted) would let 0 do.
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(bold));
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(bold));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(corps));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.ShadowPlayCard(bold);
		// The pool is 2 short of 5: one exertion is owed, and the second is the player's to make or not.
		// Toil 1 would owe two.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));

		scn.ShadowChooseCard(ted);
		assertEquals(0, scn.GetWoundsOn(corps));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 3 - (5 - 2)
		assertEquals(0, scn.GetTwilight());
		// The one possession in play is discarded without a choice.
		assertInDiscard(chain);
		assertInDiscard(bold);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
