package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_03_080_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thing", "53_80");
					put("thing2", "53_80");
					put("throw", "2_71");         // Throw Yourself in Next Time: another [moria] condition, plays on a Hobbit companion

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: unbound [gondor] Man, vitality 4
					put("merry", "1_302");        // Merry, Friend to Sam: unbound [shire] Hobbit, a skirmish ability used only on purpose
					put("guard", "1_7");          // Dwarf Guard: unbound companion, no text that matters here
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer
					put("rosie", "1_309");        // Rosie Cotton: an ally

					put("sword", "1_112");        // Ranger's Sword: [gondor] possession, bearer must be Aragorn, damage +1
					put("armor", "1_92");         // Armor: [gondor] possession, bearer must be a Man
					put("hobbitsword", "1_299");  // Hobbit Sword: [shire] possession, bearer must be a Hobbit, no other text
					put("barahir", "6_55");       // Ring of Barahir: Free Peoples artifact, bearer must be Aragorn; start-of-turn trigger only
					put("shards", "3_44");        // The Shards of Narsil: Free Peoples artifact in the support area, fellowship abilities only
					put("poison", "8_28");        // Spider Poison: a Shadow possession borne by a companion; bearer exerts when assigned

					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc, a skirmish ability used only on purpose
					put("tentacle", "101_36");    // Vile Tentacle: [moria] minion that is no Orc (a Creature); its one ability is used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SuchaLittleThingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Such a Little Thing
		 * Unique: false
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: Bearer must be an unbound companion. Limit 1 per bearer.
		 * 	At the start of each skirmish involving bearer and a [moria] minion, discard a possession borne by bearer. The Free Peoples player may exert bearer or discard another Free Peoples item to prevent that.
		 */

		var scn = GetScenario();

		var thing = scn.GetFreepsCard("thing");

		assertEquals("Such a Little Thing", thing.getBlueprint().getTitle());
		assertNull(thing.getBlueprint().getSubtitle());
		assertFalse(thing.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, thing.getBlueprint().getSide());
		assertEquals(Culture.MORIA, thing.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, thing.getBlueprint().getCardType());
		assertEquals(1, thing.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysForOneOnAnUnboundCompanionNotAlreadyBearingOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		var rosie = scn.GetFreepsCard("rosie");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, merry, guard, sam);
		scn.MoveCardsToSupportArea(rosie);

		var thing = scn.GetShadowCard("thing");
		var thing2 = scn.GetShadowCard("thing2");
		var throwYourself = scn.GetShadowCard("throw");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(thing);

		scn.StartGame();
		// The Dwarf Guard already bears one; Merry bears another [moria] condition.
		scn.AttachCardsTo(guard, thing2);
		scn.AttachCardsTo(merry, throwYourself);
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(5);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(thing));
		scn.ShadowPlayCard(thing);

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, sam, frodo, rosie, runner));
		scn.ShadowChooseCard(merry);

		assertAttachedTo(thing, merry);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheStartOfASkirmishWithAMoriaMinionAPossessionOnBearerIsDiscardedIfNotPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var shards = scn.GetFreepsCard("shards");
		var barahir = scn.GetFreepsCard("barahir");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AttachCardsTo(aragorn, sword, armor);
		scn.AttachCardsTo(merry, hobbitsword);
		scn.MoveCardsToSupportArea(shards);

		var thing = scn.GetShadowCard("thing");
		var poison = scn.GetShadowCard("poison");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, thing, poison, barahir);
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		// Spider Poison's own exertion on assignment.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(aragorn);

		// A possession borne by bearer, of either side; not one borne by another companion, nor an artifact.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, armor, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hobbitsword, shards, barahir));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sword);

		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Discard another"));
		scn.FreepsChooseOption("Let");

		assertInZone(Zone.DISCARD, sword);
		assertAttachedTo(armor, aragorn);
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(barahir, aragorn);
		assertAttachedTo(hobbitsword, merry);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMoriaMinionThatIsNoOrcSetsItOffToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var thing = scn.GetShadowCard("thing");
		var tentacle = scn.GetShadowCard("tentacle");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, thing);
		scn.MoveMinionsToTable(tentacle);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tentacle);
		assertAttachedTo(sword, aragorn);
		scn.FreepsResolveSkirmish(aragorn);

		// The one possession is chosen without asking; the Free Peoples player is asked.
		assertTrue(scn.FreepsChoiceAvailable("Let"));
		scn.FreepsChooseOption("Let");

		assertInZone(Zone.DISCARD, sword);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void FreePeoplesPlayerMayExertBearerToPreventTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var thing = scn.GetShadowCard("thing");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, thing);
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The one possession is chosen without asking; the Free Peoples player is asked.
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		scn.FreepsChooseOption("Exert");

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void FreePeoplesPlayerMayDiscardAnotherFreePeoplesItemToPreventTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var shards = scn.GetFreepsCard("shards");
		var ring = scn.GetRing();
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AttachCardsTo(aragorn, sword, armor);
		scn.AttachCardsTo(merry, hobbitsword);
		scn.MoveCardsToSupportArea(shards);

		var thing = scn.GetShadowCard("thing");
		var poison = scn.GetShadowCard("poison");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, thing);
		// A Shadow item borne by another companion.
		scn.AttachCardsTo(merry, poison);
		scn.MoveMinionsToTable(runner);
		// Vitality 4 with 3 wounds: exhausted, so he cannot be exerted to prevent it.
		scn.AddWoundsToChar(aragorn, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		scn.ShadowChooseCard(sword);

		assertFalse(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Let"));
		scn.FreepsChooseOption("Discard another");

		// Another Free Peoples item: on bearer, on another companion, an artifact in the support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, hobbitsword, shards));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, poison, ring));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(shards);

		assertInZone(Zone.DISCARD, shards);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor, aragorn);
		assertAttachedTo(hobbitsword, merry);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoWayToPreventItThePossessionIsDiscardedWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var thing = scn.GetShadowCard("thing");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, thing);
		scn.MoveMinionsToTable(runner);
		// Exhausted, and the only other Free Peoples item is The One Ring, which cannot be discarded.
		scn.AddWoundsToChar(aragorn, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.FreepsChoiceAvailable("Exert"));
		assertFalse(scn.FreepsChoiceAvailable("Discard another"));
		assertInZone(Zone.DISCARD, sword);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NothingHappensInASkirmishWithoutBothBearerAndAMoriaMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(merry, hobbitsword);

		var thing = scn.GetShadowCard("thing");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, thing);
		scn.MoveMinionsToTable(soldier, runner);

		scn.SkipToAssignments();
		// Bearer against a [sauron] Orc; a [moria] Orc against a companion who is not bearer.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{merry, runner});

		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.FreepsChoiceAvailable("Let"));
		assertAttachedTo(sword, aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(merry);
		assertFalse(scn.FreepsChoiceAvailable("Let"));
		assertAttachedTo(hobbitsword, merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoPossessionOnBearerNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AttachCardsTo(merry, hobbitsword);
		scn.MoveCardsToSupportArea(shards);

		var thing = scn.GetShadowCard("thing");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, thing);
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.FreepsChoiceAvailable("Let"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertAttachedTo(hobbitsword, merry);
		assertInZone(Zone.SUPPORT, shards);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void HappensAgainAtTheStartOfAFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var thing = scn.GetShadowCard("thing");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, thing);
		scn.MoveMinionsToTable(troop);
		// Fierce, so that bearer is in two skirmishes with it in one turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsChooseOption("Exert");
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertAttachedTo(sword, aragorn);

		// 8 against 13: Aragorn loses and takes a wound.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		scn.FreepsChooseOption("Let");
		assertInZone(Zone.DISCARD, sword);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
