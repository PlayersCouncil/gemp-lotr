package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chain", "2_53");
					put("troll", "1_165");      // Cave Troll of Moria, Scourge of the Black Pit: [moria], vitality 4; his text is about his play
					put("orctroll", "12_85");   // Cave Troll of Moria, Savage Menace: the title's [orc] card; his trigger answers an [orc] lurker's win
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text, vitality 3
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf archer companion; his archery ability is used only on purpose
					put("garrison", "8_36");    // Garrison of Gondor: a Man archer companion, vitality 3; the rest of his text is about his play and fortifications
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an archer who is an ally; his archery ability is used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion who is no archer
					put("bowmen", "3_41");      // Gondor Bowmen: Archery: exert a [gondor] companion and discard it to make the fellowship archery total +2
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf to exert for Baruk Khazad
					put("baruk", "5_5");        // Baruk Khazad: Maneuver: exert a Dwarf to wound a minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CaveTrollsChainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Cave Troll's Chain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Strength: +2
		 * Game Text: Bearer must be Cave Troll of Moria.
		 * The Free Peoples player may choose for the archery phase to be skipped.
		 * Each time Cave Troll of Moria takes a wound during the archery phase, you may wound an archer companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chain");

		assertEquals("Cave Troll's Chain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBePlayedOnCaveTrollOfMoriaAndOnNoOtherMoriaMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetShadowCard("chain");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(chain);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// A [moria] minion of another title, and a pool that could pay: no bearer.
		assertFalse(scn.ShadowPlayAvailable(chain));

		scn.MoveMinionsToTable(troll);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(chain));
		// The Troll is the only bearer it takes, so it goes on him unasked.
		scn.ShadowPlayCard(chain);
		assertAttachedTo(chain, troll);
		assertNotAttachedTo(chain, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedOnTheOrcCaveTrollOfMoria() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetShadowCard("chain");
		var orctroll = scn.GetShadowCard("orctroll");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(chain);

		scn.StartGame();
		scn.MoveMinionsToTable(orctroll, troop);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(chain));
		scn.ShadowPlayCard(chain);
		assertAttachedTo(chain, orctroll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayChooseToSkipTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetShadowCard("chain");
		var troll = scn.GetShadowCard("troll");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.AttachCardsTo(troll, chain);
		assertAttachedTo(chain, troll);

		// The maneuver phase is played as usual.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();

		// The choice is the Free Peoples player's; the Chain's owner is asked nothing.
		assertTrue(scn.FreepsDecisionAvailable("skip the Archery phase"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(0, scn.GetWoundsOn(troll));
		scn.FreepsChooseYes();

		// No archery fire: Legolas's archery wound is never placed.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troll));
	}

	@Test
	public void DecliningTheSkipLeavesTheArcheryPhaseAndAWoundOnAnotherMinionIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetShadowCard("chain");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, troop);
		scn.AttachCardsTo(troll, chain);
		assertAttachedTo(chain, troll);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("skip the Archery phase"));
		scn.FreepsChooseNo();

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();

		// Legolas's archery wound, placed by the Shadow player on the Troop: no Cave Troll of Moria was wounded.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troll, troop));
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EachArcheryWoundOnTheTrollLetsTheShadowPlayerWoundAnArcherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetShadowCard("chain");
		var troll = scn.GetShadowCard("troll");
		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, garrison, aragorn);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.AttachCardsTo(troll, chain);
		// Garrison of Gondor exhausted: a wound kills him, where an exertion would leave him out.
		scn.AddWoundsToChar(garrison, 2);
		assertAttachedTo(chain, troll);
		assertEquals(2, scn.GetWoundsOn(garrison));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsChooseNo();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();

		// Two archer companions: a fellowship archery total of 2, both wounds on the only minion.
		assertEquals(1, scn.GetWoundsOn(troll));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chain));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(garrison));

		// The second wound on the same Troll: offered again.
		assertEquals(2, scn.GetWoundsOn(troll));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chain));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, garrison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(orophin, aragorn));
		scn.ShadowChooseCard(garrison);

		assertInZone(Zone.DEAD, garrison);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnArcheryWoundOnTheOrcCaveTrollOfMoriaIsAnsweredToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetShadowCard("chain");
		var orctroll = scn.GetShadowCard("orctroll");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(orctroll);
		scn.AttachCardsTo(orctroll, chain);
		assertAttachedTo(chain, orctroll);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsChooseNo();
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(1, scn.GetWoundsOn(orctroll));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chain));
		scn.ShadowAcceptOptionalTrigger();
		// Legolas is the only archer companion: wounded unasked.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AWoundOnTheTrollInTheManeuverPhaseIsNotAnswered()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetShadowCard("chain");
		var troll = scn.GetShadowCard("troll");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveCardsToHand(baruk);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.AttachCardsTo(troll, chain);
		assertAttachedTo(chain, troll);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(gimli));
		// Gimli exerts to wound the only minion; Legolas, an archer companion, is there to be wounded.
		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(troll));

		// Baruk Khazad gives the Free Peoples player the next maneuver action; nothing waits for the Shadow player.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(legolas));
	}

	@Test
	public void WithNoArcherCompanionAnArcheryWoundOnTheTrollIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetShadowCard("chain");
		var troll = scn.GetShadowCard("troll");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var bowmen = scn.GetFreepsCard("bowmen");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToSupportArea(bowmen);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.AttachCardsTo(troll, chain);
		assertAttachedTo(chain, troll);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsChooseNo();

		// Aragorn exerts for Gondor Bowmen: a fellowship archery total of 2 with no archer companion in it.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsUseCardAction(bowmen);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetFreepsArcheryTotal());
		scn.ShadowPass();
		scn.FreepsPass();

		// Two wounds on the Troll, and nobody the Chain could wound: Orophin is an archer, but no companion.
		assertEquals(2, scn.GetWoundsOn(troll));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(orophin));
	}

	@Test
	public void AnExhaustedTrollKilledByAnArcheryWoundCannotUseTheChain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetShadowCard("chain");
		var troll = scn.GetShadowCard("troll");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.AttachCardsTo(troll, chain);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(troll, 3);
		assertAttachedTo(chain, troll);
		assertEquals(3, scn.GetWoundsOn(troll));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsChooseNo();
		scn.FreepsPass();
		scn.ShadowPass();

		// Legolas's archery wound kills the Troll, and the Chain goes with him before it can be used.
		assertInDiscard(troll, chain);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
