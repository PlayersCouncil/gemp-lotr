package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_074_Tests
{

	// The usual Fellowship sites: none below site 4 is underground, where this Balrog cannot be exerted.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("whip", "2_74");
					// The Balrog, The Terror of Khazad-dum: vitality 5; its text (fierce, no wounds, no exertion)
					// acts only at an underground site.
					put("balrog", "12_79");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion of another title, vitality 3, no text
					put("champion", "11_111");  // Champion Orc: an [orc] Orc, toil 2 and nothing else
					put("gear", "17_90");       // Rider's Gear: a Shadow armor on an [orc] Orc; bars wounds only on a mounted bearer

					// Household Guard: a [rohan] Man with valiant and nothing else, and what he bears
					put("guard", "5_83");
					put("helm", "5_89");        // Rohirrim Helm: helm; a skirmish ability only used on purpose
					put("shield", "5_91");      // Rohirrim Shield: shield; minion archery total -1, a regroup ability
					put("chain", "4_278");      // Heavy Chain: armor, bearer a Man; not overwhelmed unless tripled
					put("sword", "4_291");      // Sword of Rohan: a hand weapon; damage +1 against an Uruk-hai only

					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally who may take part in skirmishes
					put("dwarvenarmor", "1_8"); // Dwarven Armor: armor, bearer a Dwarf; not overwhelmed unless tripled
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only used on purpose
					put("armor", "1_92");       // Armor: [gondor] armor, bearer a Man; caps wounds in skirmishes
					put("mithril", "2_105");    // Mithril-coat: an armor that is an ARTIFACT, bearer Frodo
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhipofManyThongsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Whip of Many Thongs
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Strength: +1
		 * Game Text: Bearer must be The Balrog.<br><b>Skirmish:</b> Exert The Balrog to discard a helm, shield, or armor borne by a character The Balrog is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("whip");

		assertEquals("Whip of Many Thongs", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void MayBePlayedOnTheBalrogAndOnNoOtherMoriaMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whip = scn.GetShadowCard("whip");
		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(whip);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(whip));

		scn.MoveMinionsToTable(balrog);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(whip));
		// The Balrog is unique, so he is the one bearer there can be, and the bearer is not asked for.
		scn.ShadowPlayCard(whip);

		assertAttachedTo(whip, balrog);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExertsTheBalrogToDiscardAHelmShieldOrArmorBorneByTheCharacterHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whip = scn.GetShadowCard("whip");
		var balrog = scn.GetShadowCard("balrog");
		var champion = scn.GetShadowCard("champion");
		var gear = scn.GetShadowCard("gear");
		var guard = scn.GetFreepsCard("guard");
		var helm = scn.GetFreepsCard("helm");
		var shield = scn.GetFreepsCard("shield");
		var chain = scn.GetFreepsCard("chain");
		var sword = scn.GetFreepsCard("sword");

		scn.StartGame();
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, helm, shield, chain, sword);
		scn.MoveMinionsToTable(balrog, champion);
		scn.AttachCardsTo(balrog, whip);
		scn.AttachCardsTo(champion, gear);
		assertAttachedTo(helm, guard);
		assertAttachedTo(shield, guard);
		assertAttachedTo(chain, guard);
		assertAttachedTo(sword, guard);
		assertAttachedTo(whip, balrog);
		assertAttachedTo(gear, champion);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, balrog);
		// The Orc left over is put in the same skirmish by the Shadow player.
		scn.ShadowAssignToMinions(guard, champion);
		scn.FreepsResolveSkirmish(guard);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(balrog));
		assertTrue(scn.ShadowActionAvailable(whip));
		scn.ShadowUseCardAction(whip);
		assertEquals(1, scn.GetWoundsOn(balrog));

		// The three classes on the Guard; not his hand weapon, nor the armor of the other minion in the skirmish.
		assertTrue(scn.ShadowHasCardChoiceAvailable(helm, shield, chain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, gear));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(shield);

		assertInDiscard(shield);
		assertAttachedTo(helm, guard);
		assertAttachedTo(chain, guard);
		assertAttachedTo(sword, guard);
		assertAttachedTo(gear, champion);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherSkirmishItDiscardsNothingAndInHisOwnItReachesTheAllyHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whip = scn.GetShadowCard("whip");
		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		var thrarin = scn.GetFreepsCard("thrarin");
		var dwarvenarmor = scn.GetFreepsCard("dwarvenarmor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, armor);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, dwarvenarmor);
		scn.MoveMinionsToTable(balrog, troop);
		scn.AttachCardsTo(balrog, whip);
		assertAttachedTo(armor, aragorn);
		assertAttachedTo(dwarvenarmor, thrarin);
		assertAttachedTo(whip, balrog);

		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{thrarin, balrog});

		// Aragorn's skirmish first: the Balrog is waiting for Thrarin.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(balrog));
		assertTrue(scn.ShadowActionAvailable(whip));
		scn.ShadowUseCardAction(whip);

		// Paid for nothing: Aragorn is fighting another minion, and Thrarin's skirmish has not begun.
		assertEquals(1, scn.GetWoundsOn(balrog));
		assertAttachedTo(armor, aragorn);
		assertAttachedTo(dwarvenarmor, thrarin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		scn.ShadowUseCardAction(whip);

		// The ally he is skirmishing: his armor is the one there is, and goes without a choice.
		assertEquals(2, scn.GetWoundsOn(balrog));
		assertInDiscard(dwarvenarmor);
		assertAttachedTo(armor, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ItDiscardsAnArtifactArmorAndIsNotOfferedOnceTheBalrogIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whip = scn.GetShadowCard("whip");
		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var mithril = scn.GetFreepsCard("mithril");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, mithril);
		scn.MoveMinionsToTable(balrog, troop);
		scn.AttachCardsTo(balrog, whip);
		// Vitality 5 with 3 wounds: one exertion left in him.
		scn.AddWoundsToChar(balrog, 3);
		assertAttachedTo(mithril, frodo);
		assertAttachedTo(whip, balrog);
		assertEquals(3, scn.GetWoundsOn(balrog));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, balrog}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		scn.ShadowUseCardAction(whip);
		assertEquals(4, scn.GetWoundsOn(balrog));
		assertInDiscard(mithril);

		scn.FreepsPass();

		// Exhausted, the Balrog cannot pay; the Goblin Patrol Troop could exert, and is no Balrog.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(whip));
		assertEquals(0, scn.GetWoundsOn(troop));
	}
}
